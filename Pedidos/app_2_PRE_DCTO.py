from flask import Flask, render_template, request, redirect, url_for, session, flash, jsonify
import mysql.connector
import time
import random
import datetime
from functools import wraps


app = Flask(__name__)
app.secret_key = 'tu_super_secreto_key_aqui' # ¡Cambia esto en producción!

# Configuración de la base de datos
db_config = {
    'host': '192.168.35.25',
    'user': 'mmarco',         # Reemplaza con tu usuario
    'password': '@System345', # Reemplaza con tu contraseña
    'database': 'pract'  # El nombre de tu BD
}

def get_db_connection():
    """Establece conexión con la base de datos."""
    try:
        conn = mysql.connector.connect(**db_config)
        return conn
    except mysql.connector.Error as err:
        flash(f"Error de conexión a la base de datos: {err}", "danger")
        # Podrías loggear el error aquí también para el servidor
        print(f"ERROR DB Connection: {err}")
        return None

def login_required(f):
    """Decorador para requerir login en ciertas rutas."""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'user_id' not in session:
            flash("Debes iniciar sesión para acceder a esta página.", "warning")
            return redirect(url_for('login', next=request.url))
        return f(*args, **kwargs)
    return decorated_function

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']

        conn = get_db_connection()
        if not conn:
            return render_template('login.html')

        cursor = conn.cursor(dictionary=True)
        user = None
        try:
            # IMPORTANTE: ¡Esta es una comparación de texto plano, insegura!
            # En producción, usa werkzeug.security.check_password_hash(user['password_hash'], password)
            # Y almacena hashes, no contraseñas en texto plano.
            cursor.execute("SELECT * FROM USUARIOS_LOGIN WHERE COD_USER = %s AND PWD = %s", (username, password))
            user = cursor.fetchone()
        except mysql.connector.Error as err:
            flash(f"Error al consultar usuario: {err}", "danger")
            print(f"ERROR DB Login query: {err}")
        finally:
            cursor.close()
            conn.close()

        if user:
            session['user_id'] = user['COD_USER'] # Guardamos el username como identificador
            flash(f"Bienvenid@, {user['NOMBRE']}!", "success")
            next_url = request.args.get('next')
            return redirect(next_url or url_for('index'))
        else:
            flash("Usuario o contraseña incorrectos.", "danger")
    return render_template('login.html')

@app.route('/logout')
def logout():
    session.pop('user_id', None)
    flash("Has cerrado sesión.", "info")
    return redirect(url_for('login'))

@app.route('/')
@login_required
def index():
    conn = get_db_connection()
    if not conn:
        return render_template('pedidos_lista.html', pedidos_para_mostrar={}) # Pasar dict vacío

    cursor = conn.cursor(dictionary=True)
    pedidos_db = []
    try:
        # Modificar la consulta para incluir PED.ESTADO
        cursor.execute("""
            SELECT 
                pw.ID AS PED_ID, pw.ARTI, pw.CANTIDAD, pw.COD_CTE, pw.COD_DIR, 
                pw.FECHA_CREACION, pw.FECHA_EXP, pw.PEDIDO_CTE, pw.USUARIO, 
                pw.NUMPED, pw.ESTADO,  -- <<< AÑADIDO ESTADO
                c.RAZON_SOCIAL,
                itm.DESCRIPCION AS DESCRIPCION_ARTI,
                itm.UD_EMB,
                itm.PRECIO AS PRECIO_ARTI,
                itm.STOCK_FAM
            FROM PEDIDOS_WEB pw
            LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE
            LEFT JOIN ITMMASTER itm ON pw.ARTI = itm.COD_ART
            WHERE pw.USUARIO = %s 
            ORDER BY pw.NUMPED DESC, pw.ID ASC
        """, (session['user_id'],))
        pedidos_db = cursor.fetchall()
    except mysql.connector.Error as err:
        flash(f"Error al obtener pedidos: {err}", "danger")
        print(f"ERROR DB Fetching pedidos: {err}")
    finally:
        if conn.is_connected():
            if cursor and hasattr(cursor, 'close') and callable(getattr(cursor, 'close')): # Más seguro
                 try: cursor.close()
                 except Exception as e: print(f"Error cerrando cursor (index): {e}")
            conn.close()

    pedidos_agrupados = {}
    if pedidos_db:
        for linea_pedido in pedidos_db:
            numped = linea_pedido['NUMPED']
            if numped not in pedidos_agrupados:
                pedidos_agrupados[numped] = {
                    'cabecera': {
                        'NUMPED': linea_pedido['NUMPED'],
                        'COD_CTE': linea_pedido['COD_CTE'],
                        'RAZON_SOCIAL': linea_pedido.get('RAZON_SOCIAL', ''), # Usar .get() por si el JOIN falla
                        'COD_DIR': linea_pedido['COD_DIR'],
                        'FECHA_CREACION': linea_pedido['FECHA_CREACION'],
                        'FECHA_EXP': linea_pedido['FECHA_EXP'],
                        'PEDIDO_CTE': linea_pedido['PEDIDO_CTE'],
                        'USUARIO': linea_pedido['USUARIO'],
                        'ESTADO': linea_pedido.get('ESTADO', 'Desconocido') # <<< AÑADIDO ESTADO A CABECERA
                    },
                    'lineas': []
                }
            pedidos_agrupados[numped]['lineas'].append({
                'ID_LINEA': linea_pedido['PED_ID'],
                'ARTI': linea_pedido['ARTI'],
                'DESCRIPCION_ARTI': linea_pedido.get('DESCRIPCION_ARTI', ''),
                'CANTIDAD': linea_pedido['CANTIDAD'],
                'PRECIO_ARTI': linea_pedido.get('PRECIO_ARTI'),
                'UD_EMB': linea_pedido.get('UD_EMB'),
                'STOCK_FAM_ARTI': linea_pedido.get('STOCK_FAM')
            })
    
    return render_template('pedidos_lista.html', pedidos_para_mostrar=pedidos_agrupados)


@app.route('/pedidos/nuevo', methods=['GET', 'POST'])
@login_required
def agregar_pedido():
    if request.method == 'POST':
        # --- RECOGER DATOS DE CABECERA DEL FORMULARIO ---
        cod_cte_form = request.form.get('COD_CTE', '').strip()
        cod_dir_form = request.form.get('COD_DIR', '').strip()
        fecha_exp_str_form = request.form.get('FECHA_EXP', '').strip()
        pedido_cte_ref_form = request.form.get('PEDIDO_CTE', '').strip()
        observaciones_form = request.form.get('OBSERVACIONES', '').strip()
        usuario_actual = session['user_id']

        # Determinar el estado del pedido a partir del checkbox
        estado_pedido = "BLOQUEADO" if 'bloqueado' in request.form else "PENDIENTE"
        
        # Parsear líneas del formulario ANTES de las validaciones de cabecera
        # para poder repoblar todo si algo falla.
        parsed_lineas_form_error = {}
        lineas_para_repoblar = []
        for key, value in request.form.items():
            if key.startswith('lineas['):
                parts = key.replace(']', '').split('[')
                try:
                    idx = int(parts[1])
                    field_name = parts[2]
                    if idx not in parsed_lineas_form_error:
                        parsed_lineas_form_error[idx] = {}
                    # Guardar también el ID_LINEA si viniera, aunque para nuevo pedido sería vacío
                    parsed_lineas_form_error[idx][field_name] = value
                except (IndexError, ValueError):
                    print(f"Advertencia: Clave de línea malformada ignorada: {key}")
                    continue
        
        for idx in sorted(parsed_lineas_form_error.keys()):
            # Asegurar que la línea tenga al menos ARTI y CANTIDAD para ser considerada una línea a repoblar
            if 'ARTI' in parsed_lineas_form_error[idx] or 'CANTIDAD' in parsed_lineas_form_error[idx]:
                 lineas_para_repoblar.append(parsed_lineas_form_error[idx])


        pedido_cabecera_para_repoblar = {
            'COD_CTE': cod_cte_form, 'COD_DIR': cod_dir_form,
            'FECHA_EXP': fecha_exp_str_form, 'PEDIDO_CTE': pedido_cte_ref_form,
            'OBSERVACIONES': observaciones_form
        }

        # --- VALIDACIONES DE CABECERA ---
        if len(observaciones_form) > 30:
            flash("El campo Observaciones no puede tener más de 30 caracteres.", "danger")
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        if not cod_cte_form:
            flash("El Código de Cliente es obligatorio.", "danger")
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        conn_val_cte = get_db_connection()
        if not conn_val_cte: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
        cursor_val_cte = conn_val_cte.cursor(dictionary=True)
        cliente_existe = False
        try:
            cursor_val_cte.execute("SELECT COD_CTE FROM CLIENTES_WEB WHERE COD_CTE = %s", (cod_cte_form,))
            if cursor_val_cte.fetchone(): cliente_existe = True
            else: flash(f"El Código de Cliente '{cod_cte_form}' no existe.", "danger")
        except mysql.connector.Error as err: flash(f"Error al validar cliente: {err}", "danger")
        finally: 
            if conn_val_cte.is_connected(): cursor_val_cte.close(); conn_val_cte.close()
        if not cliente_existe: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        direccion_valida = True 
        if cod_dir_form: 
            conn_val_dir = get_db_connection()
            if not conn_val_dir: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            cursor_val_dir = conn_val_dir.cursor(dictionary=True)
            try:
                cursor_val_dir.execute("SELECT COD_DIR FROM DIRECCIONES_CTE_WEB WHERE COD_CTE = %s AND COD_DIR = %s", (cod_cte_form, cod_dir_form))
                if not cursor_val_dir.fetchone():
                    flash(f"El Código de Dirección '{cod_dir_form}' no es válido para el cliente '{cod_cte_form}'.", "danger")
                    direccion_valida = False
            except mysql.connector.Error as err: flash(f"Error al validar dirección: {err}", "danger"); direccion_valida = False
            finally: 
                if conn_val_dir.is_connected(): cursor_val_dir.close(); conn_val_dir.close()
            if not direccion_valida: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        fecha_exp_obj = None
        if fecha_exp_str_form:
            try:
                fecha_exp_obj = datetime.datetime.strptime(fecha_exp_str_form, '%Y-%m-%d').date()
            except ValueError:
                flash("Formato de Fecha de Expiración inválido. Usar YYYY-MM-DD.", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        # --- PROCESAR Y VALIDAR LÍNEAS ---
        lineas_procesadas_ok = [] 
        if not lineas_para_repoblar:
             flash("Debe agregar al menos una línea al pedido.", "danger")
             return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        for idx, linea_form_data in enumerate(lineas_para_repoblar):
            # *** INICIALIZACIÓN DENTRO DEL BUCLE DE LÍNEAS ***
            ud_emb_articulo = None 
            articulo_linea_existe = False
            # *** FIN INICIALIZACIÓN ***

            arti_linea = linea_form_data.get('ARTI', '').strip()
            cantidad_str_linea = linea_form_data.get('CANTIDAD', '').strip()

            if not arti_linea:
                flash(f"El campo Artículo es obligatorio para todas las líneas (error en línea aprox. {idx + 1}).", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            
            conn_val_arti = get_db_connection()
            if not conn_val_arti: 
                flash("Error de conexión al validar artículo de línea.", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            
            cursor_val_arti = conn_val_arti.cursor(dictionary=True)
            try:
                cursor_val_arti.execute("SELECT COD_ART, UD_EMB FROM ITMMASTER WHERE COD_ART = %s", (arti_linea,))
                item_maestro = cursor_val_arti.fetchone()
                if item_maestro:
                    articulo_linea_existe = True
                    ud_emb_articulo = item_maestro.get('UD_EMB') 
                    # Validar que UD_EMB sea un número usable para la lógica de múltiplo
                    if ud_emb_articulo is not None:
                        try:
                            temp_ud_emb = int(float(ud_emb_articulo)) # Manejar Decimal y convertir a int
                            if temp_ud_emb <= 0:
                                print(f"Advertencia: UD_EMB para artículo '{arti_linea}' es <= 0 ({ud_emb_articulo}). No se validará múltiplo.")
                                ud_emb_articulo = None 
                            else:
                                ud_emb_articulo = temp_ud_emb # Usar el entero validado
                        except (ValueError, TypeError):
                            print(f"Advertencia: UD_EMB para artículo '{arti_linea}' no es un número válido ({ud_emb_articulo}). No se validará múltiplo.")
                            ud_emb_articulo = None
                    else: # Si UD_EMB es None desde la BD
                        ud_emb_articulo = None
                else:
                    flash(f"Artículo '{arti_linea}' inválido en línea aprox. {idx + 1}.", "danger")
                    # ud_emb_articulo y articulo_linea_existe ya son None/False por inicialización
            except mysql.connector.Error as err:
                flash(f"Error al validar artículo de línea {idx+1}: {err}", "danger")
                # ud_emb_articulo y articulo_linea_existe ya son None/False
            finally:
                if conn_val_arti.is_connected(): 
                    cursor_val_arti.close()
                    conn_val_arti.close()

            if not articulo_linea_existe:
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            # DEBUG ANTES DE LA LÍNEA PROBLEMÁTICA
            # print(f"DEBUG: Línea {idx+1}, arti_linea: {arti_linea}")
            # print(f"DEBUG: ud_emb_articulo: {ud_emb_articulo}, tipo: {type(ud_emb_articulo)}")
            # print(f"DEBUG: cantidad_str_linea: {cantidad_str_linea}")

            if not cantidad_str_linea:
                 flash(f"La Cantidad es obligatoria para todas las líneas (error en línea aprox. {idx + 1}).", "danger")
                 return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            try:
                cantidad_linea = int(cantidad_str_linea)
                if cantidad_linea <= 0:
                    raise ValueError("Cantidad debe ser positiva")

                # VALIDACIÓN: MÚLTIPLO DE UD_EMB
                if ud_emb_articulo and ud_emb_articulo > 0: # ud_emb_articulo ya debería ser un int > 0 o None
                    if cantidad_linea % ud_emb_articulo != 0:
                        flash(f"La cantidad ({cantidad_linea}) para el artículo '{arti_linea}' en la línea aprox. {idx + 1} "
                              f"debe ser un múltiplo de su unidad de embalaje ({ud_emb_articulo}).", "danger")
                        return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            except ValueError as e: 
                flash(f"Cantidad inválida ('{cantidad_str_linea}') en línea aprox. {idx + 1}: {e}", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            
            lineas_procesadas_ok.append({'ARTI': arti_linea, 'CANTIDAD': cantidad_linea})

        if not lineas_procesadas_ok:
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

        # --- SI TODAS LAS VALIDACIONES PASAN, GENERAR NUMPED E INSERTAR ---
        numped_nuevo = generar_numped()
        fecha_creacion_actual = datetime.datetime.now() 

        conn_insert = get_db_connection()
        if not conn_insert:
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
        
        cursor_insert = conn_insert.cursor()
        sql_insert_linea = """
            INSERT INTO PEDIDOS_WEB (NUMPED, ARTI, CANTIDAD, COD_CTE, COD_DIR,
                                     FECHA_CREACION, FECHA_EXP, PEDIDO_CTE, USUARIO, ESTADO, OBSERVACIONES)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        try:
            for linea_p_ok in lineas_procesadas_ok:
                val = (
                    numped_nuevo,
                    linea_p_ok['ARTI'],
                    linea_p_ok['CANTIDAD'],
                    cod_cte_form if cod_cte_form else None,
                    cod_dir_form if cod_dir_form else None,
                    fecha_creacion_actual, 
                    fecha_exp_obj, 
                    pedido_cte_ref_form if pedido_cte_ref_form else None,
                    usuario_actual,
                    estado_pedido,
                    observaciones_form if observaciones_form else None
                )
                cursor_insert.execute(sql_insert_linea, val)
            
            conn_insert.commit()
            flash(f"Pedido {numped_nuevo} creado exitosamente con {len(lineas_procesadas_ok)} líneas.", "success")
            return redirect(url_for('index'))

        except mysql.connector.Error as err:
            conn_insert.rollback()
            flash(f"Error al guardar el pedido en la base de datos: {err}", "danger")
            print(f"ERROR DB creating multi-line order: {err}")
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
        finally:
            if conn_insert.is_connected():
                cursor_insert.close()
                conn_insert.close()

    # --- MÉTODO GET: Mostrar formulario vacío o con valores por defecto ---
    else: 
        return render_template('pedido_form.html', 
                               pedido_cabecera={}, 
                               lineas_pedido=[{'ARTI':'', 'CANTIDAD':'', 'ID_LINEA':'', 'DESCRIPCION_ARTI':'', 'PRECIO_ARTI': '0.00', 'SOTCK_FAM_ARTI': 'N/A'}])

@app.route('/pedidos/editar/<numped_a_editar>', methods=['GET', 'POST'])
@login_required
def editar_pedido(numped_a_editar):
    usuario_actual = session['user_id']

    if request.method == 'POST':
        # --- INICIO RAMA POST ---
        conn_val_cte, cursor_val_cte = None, None
        conn_val_dir, cursor_val_dir = None, None
        conn_update, cursor_update = None, None
        
        parsed_lineas_form_error = {}
        lineas_para_repoblar = []

        try:
            # --- RECOGER Y PARSEAR DATOS DEL FORMULARIO ---
            cod_cte_form = request.form.get('COD_CTE', '').strip()
            cod_dir_form = request.form.get('COD_DIR', '').strip()
            fecha_exp_str_form = request.form.get('FECHA_EXP', '').strip()
            pedido_cte_ref_form = request.form.get('PEDIDO_CTE', '').strip()
            observaciones_form = request.form.get('OBSERVACIONES', '').strip()

            # Determinar el estado del pedido a partir del checkbox
            estado_pedido = "BLOQUEADO" if 'bloqueado' in request.form else "PENDIENTE"
            
            for key, value in request.form.items(): # Parseo de líneas
                if key.startswith('lineas['):
                    parts = key.replace(']', '').split('[')
                    try:
                        idx = int(parts[1])
                        field_name = parts[2]
                        if idx not in parsed_lineas_form_error: parsed_lineas_form_error[idx] = {}
                        parsed_lineas_form_error[idx][field_name] = value
                    except (IndexError, ValueError): print(f"Advertencia Edición POST: Clave línea malformada: {key}")
            
            for idx in sorted(parsed_lineas_form_error.keys()):
                if 'ARTI' in parsed_lineas_form_error[idx] or 'CANTIDAD' in parsed_lineas_form_error[idx]:
                     lineas_para_repoblar.append(parsed_lineas_form_error[idx])

            pedido_cabecera_para_repoblar = {
                'NUMPED': numped_a_editar, 'COD_CTE': cod_cte_form, 'COD_DIR': cod_dir_form,
                'FECHA_EXP': fecha_exp_str_form, 'PEDIDO_CTE': pedido_cte_ref_form,
                'OBSERVACIONES': observaciones_form
            }

            # --- VALIDACIONES DE CABECERA ---
            if len(observaciones_form) > 30:
                flash("El campo Observaciones no puede tener más de 30 caracteres.", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            if not cod_cte_form:
                flash("El Código de Cliente es obligatorio.", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            conn_val_cte = get_db_connection()
            if not conn_val_cte: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            cursor_val_cte = conn_val_cte.cursor(dictionary=True)
            cursor_val_cte.execute("SELECT COD_CTE FROM CLIENTES_WEB WHERE COD_CTE = %s", (cod_cte_form,))
            if not cursor_val_cte.fetchone():
                flash(f"El Código de Cliente '{cod_cte_form}' no existe.", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            # Cerrar recursos de validación de cliente
            if cursor_val_cte: cursor_val_cte.close(); cursor_val_cte = None 
            if conn_val_cte and conn_val_cte.is_connected(): conn_val_cte.close(); conn_val_cte = None

            if cod_dir_form: # Solo validar si se proporcionó COD_DIR
                conn_val_dir = get_db_connection()
                if not conn_val_dir: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
                cursor_val_dir = conn_val_dir.cursor(dictionary=True)
                cursor_val_dir.execute("SELECT COD_DIR FROM DIRECCIONES_CTE_WEB WHERE COD_CTE = %s AND COD_DIR = %s", (cod_cte_form, cod_dir_form))
                if not cursor_val_dir.fetchone():
                    flash(f"Dir. '{cod_dir_form}' no válida para cliente '{cod_cte_form}'.", "danger")
                    return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
                # Cerrar recursos de validación de dirección
                if cursor_val_dir: cursor_val_dir.close(); cursor_val_dir = None
                if conn_val_dir and conn_val_dir.is_connected(): conn_val_dir.close(); conn_val_dir = None
            
            fecha_exp_obj = None
            if fecha_exp_str_form:
                try: fecha_exp_obj = datetime.datetime.strptime(fecha_exp_str_form, '%Y-%m-%d').date()
                except ValueError:
                    flash("Formato de Fecha de Expiración inválido.", "danger")
                    return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            # --- PROCESAR Y VALIDAR LÍNEAS DEL FORMULARIO ---
            lineas_procesadas_ok_edit = []
            if not lineas_para_repoblar: # Si no se envió ninguna línea (ej. todas eliminadas)
                 flash("Debe haber al menos una línea en el pedido.", "danger")
                 return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            for idx, linea_form_data in enumerate(lineas_para_repoblar):
                ud_emb_articulo, articulo_linea_existe = None, False
                arti_linea = linea_form_data.get('ARTI', '').strip()
                cantidad_str_linea = linea_form_data.get('CANTIDAD', '').strip()
                conn_val_arti_l, cursor_val_arti_l = None, None 

                if not arti_linea:
                    flash(f"Artículo obligatorio (línea {idx + 1}).", "danger")
                    return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
                try:
                    conn_val_arti_l = get_db_connection()
                    if not conn_val_arti_l: flash("Error conexión validando art. línea.", "danger"); raise Exception("DB conn fail for line item")
                    cursor_val_arti_l = conn_val_arti_l.cursor(dictionary=True)
                    cursor_val_arti_l.execute("SELECT COD_ART, DESCRIPCION, UD_EMB FROM ITMMASTER WHERE COD_ART = %s", (arti_linea,))
                    item_maestro = cursor_val_arti_l.fetchone()
                    if item_maestro:
                        articulo_linea_existe = True
                        ud_emb_articulo_raw = item_maestro.get('UD_EMB')
                        # DESCRIPCION no se usa aquí en POST, solo UD_EMB para validación
                        if ud_emb_articulo_raw is not None:
                            try:
                                temp_ud_emb = int(float(ud_emb_articulo_raw))
                                ud_emb_articulo = temp_ud_emb if temp_ud_emb > 0 else None
                            except (ValueError, TypeError): ud_emb_articulo = None
                    else: flash(f"Artículo '{arti_linea}' inválido (línea {idx + 1}).", "danger")
                except Exception as e_arti_val:
                    flash(f"Error validando artículo línea {idx+1}: {e_arti_val}", "danger")
                finally:
                    if cursor_val_arti_l: cursor_val_arti_l.close()
                    if conn_val_arti_l and conn_val_arti_l.is_connected(): conn_val_arti_l.close()
                
                if not articulo_linea_existe: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

                if not cantidad_str_linea:
                    flash(f"Cantidad obligatoria (línea {idx + 1}).", "danger")
                    return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
                try:
                    cantidad_linea = int(cantidad_str_linea)
                    if cantidad_linea <= 0: raise ValueError("Cantidad debe ser > 0")
                    if ud_emb_articulo and cantidad_linea % ud_emb_articulo != 0:
                        flash(f"Cant. ({cantidad_linea}) art. '{arti_linea}' (línea {idx+1}) debe ser múltiplo de {ud_emb_articulo}.", "danger")
                        return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
                except ValueError as e_cant:
                    flash(f"Cantidad inválida ('{cantidad_str_linea}') línea {idx + 1}: {e_cant}", "danger")
                    return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
                
                lineas_procesadas_ok_edit.append({'ARTI': arti_linea, 'CANTIDAD': cantidad_linea})

            if not lineas_procesadas_ok_edit: # Si ninguna línea pasó la validación o se enviaron vacías
                # Este flash puede ser redundante si los errores de línea ya se mostraron.
                # flash("No se procesaron líneas válidas para el pedido.", "danger")
                return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

            # --- SI TODAS LAS VALIDACIONES PASAN, ACTUALIZAR EN BD ---
            conn_update = get_db_connection()
            if not conn_update: return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
            cursor_update = conn_update.cursor()
            
            cursor_update.execute("SELECT USUARIO, FECHA_CREACION FROM PEDIDOS_WEB WHERE NUMPED = %s AND USUARIO = %s LIMIT 1", 
                               (numped_a_editar, usuario_actual))
            pedido_original_info_tuple = cursor_update.fetchone() 
            if not pedido_original_info_tuple:
                flash("Pedido a editar no encontrado o sin permiso.", "warning")
                return redirect(url_for('index')) 
            
            fecha_creacion_original = pedido_original_info_tuple[1] 
            cursor_update.execute("DELETE FROM PEDIDOS_WEB WHERE NUMPED = %s AND USUARIO = %s", (numped_a_editar, usuario_actual))

            sql_reinsert = "INSERT INTO PEDIDOS_WEB (NUMPED, ARTI, CANTIDAD, COD_CTE, COD_DIR, FECHA_CREACION, FECHA_EXP, PEDIDO_CTE, USUARIO, ESTADO, OBSERVACIONES) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
            for linea_p_ok in lineas_procesadas_ok_edit:
                val_reinsert = (
                    numped_a_editar, linea_p_ok['ARTI'], linea_p_ok['CANTIDAD'],
                    cod_cte_form or None, cod_dir_form or None, fecha_creacion_original,
                    fecha_exp_obj, pedido_cte_ref_form or None, usuario_actual, estado_pedido,
                    observaciones_form if observaciones_form else None
                )
                cursor_update.execute(sql_reinsert, val_reinsert)
            
            conn_update.commit()
            flash(f"Pedido {numped_a_editar} actualizado.", "success")
            return redirect(url_for('index'))

        except mysql.connector.Error as db_err:
            if conn_update and conn_update.is_connected(): conn_update.rollback()
            flash(f"Error de BD al editar el pedido: {db_err}", "danger")
            print(f"ERROR DB (POST editar_pedido): {db_err}")
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
        except Exception as e:
            if conn_update and conn_update.is_connected(): conn_update.rollback()
            flash(f"Error inesperado al editar el pedido: {e}", "danger")
            print(f"ERROR General (POST editar_pedido): {type(e).__name__} - {e}")
            return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)
        finally:
            # Cerrar conexiones de validación de cabecera POST (si se abrieron y no se cerraron antes)
            if cursor_val_cte: 
                try: cursor_val_cte.close()
                except Exception as e: print(f"Error cerrando cursor_val_cte (POST): {e}")
            if conn_val_cte and conn_val_cte.is_connected(): 
                try: conn_val_cte.close()
                except Exception as e: print(f"Error cerrando conn_val_cte (POST): {e}")
            
            if cursor_val_dir: 
                try: cursor_val_dir.close()
                except Exception as e: print(f"Error cerrando cursor_val_dir (POST): {e}")
            if conn_val_dir and conn_val_dir.is_connected(): 
                try: conn_val_dir.close()
                except Exception as e: print(f"Error cerrando conn_val_dir (POST): {e}")

            # Cerrar conexión de actualización POST
            if cursor_update: 
                try: cursor_update.close()
                except Exception as e: print(f"Error cerrando cursor_update (POST): {e}")
            if conn_update and conn_update.is_connected(): 
                try: conn_update.close()
                except Exception as e: print(f"Error cerrando conn_update (POST): {e}")
        
        # Fallback en caso de que alguna lógica no retorne explícitamente
        return render_template('pedido_form.html', pedido_cabecera=pedido_cabecera_para_repoblar, lineas_pedido=lineas_para_repoblar)

    # --- MÉTODO GET: Cargar datos para el formulario de edición ---
    else: 
        conn_get = None 
        cursor_get = None
        try:
            conn_get = get_db_connection()
            if not conn_get: return redirect(url_for('index'))
            cursor_get = conn_get.cursor(dictionary=True)
            
            # Modificamos la consulta para incluir DESCRIPCION y UD_EMB de ITMMASTER
            cursor_get.execute("""
                SELECT pw.ID, pw.NUMPED, pw.ARTI, pw.CANTIDAD, pw.COD_CTE, pw.COD_DIR,
                       pw.FECHA_CREACION, pw.FECHA_EXP, pw.PEDIDO_CTE, pw.USUARIO, pw.ESTADO, pw.OBSERVACIONES,
                       itm.DESCRIPCION AS DESCRIPCION_ARTI, 
                       itm.UD_EMB,
                       itm.PRECIO AS PRECIO_ARTI,
                       itm.STOCK_FAM
                FROM PEDIDOS_WEB pw
                LEFT JOIN ITMMASTER itm ON pw.ARTI = itm.COD_ART
                WHERE pw.NUMPED = %s AND pw.USUARIO = %s 
                ORDER BY pw.ID ASC
            """, (numped_a_editar, usuario_actual))
            lineas_pedido_db_get = cursor_get.fetchall()

            if not lineas_pedido_db_get:
                flash("Pedido no encontrado o no tienes acceso para editarlo.", "warning")
                return redirect(url_for('index'))

            cabecera_db_get = lineas_pedido_db_get[0]
            pedido_cabecera_para_form_get = {
                'NUMPED': cabecera_db_get['NUMPED'],
                'COD_CTE': cabecera_db_get['COD_CTE'] or '',
                'COD_DIR': cabecera_db_get['COD_DIR'] or '',
                'FECHA_EXP': cabecera_db_get['FECHA_EXP'].strftime('%Y-%m-%d') if cabecera_db_get['FECHA_EXP'] else '',
                'PEDIDO_CTE': cabecera_db_get['PEDIDO_CTE'] or '',
                'ESTADO': cabecera_db_get.get('ESTADO'),
                'OBSERVACIONES': cabecera_db_get.get('OBSERVACIONES') or ''
            }
            lineas_para_form_get = []
            for linea_db_get in lineas_pedido_db_get:
                lineas_para_form_get.append({
                    'ID_LINEA': linea_db_get['ID'], 
                    'ARTI': linea_db_get['ARTI'],
                    'CANTIDAD': str(linea_db_get['CANTIDAD']),
                    'DESCRIPCION_ARTI': linea_db_get.get('DESCRIPCION_ARTI', ''), # Usar .get() por si el JOIN no trae descripción
                    'UD_EMB': linea_db_get.get('UD_EMB'), # Usar .get() por si el JOIN no trae UD_EMB
                    'PRECIO_ARTI': linea_db_get.get('PRECIO_ARTI'),
                    'STOCK_FAM_ARTI': linea_db_get.get('STOCK_FAM')
                })
            
            return render_template('pedido_form.html', 
                                   pedido_cabecera=pedido_cabecera_para_form_get, 
                                   lineas_pedido=lineas_para_form_get)

        except mysql.connector.Error as db_err_get:
            flash(f"Error de BD al cargar pedido para editar: {db_err_get}", "danger")
            print(f"ERROR DB (GET editar_pedido): {db_err_get}")
            return redirect(url_for('index'))
        except Exception as e_get:
            flash(f"Error inesperado al cargar pedido para editar: {e_get}", "danger")
            print(f"ERROR General (GET editar_pedido): {type(e_get).__name__} - {e_get}")
            return redirect(url_for('index'))
        finally:
            if cursor_get: 
                try: cursor_get.close()
                except Exception as e_cursor: print(f"Error cerrando cursor GET: {e_cursor}")
            if conn_get and conn_get.is_connected():
                try: conn_get.close()
                except Exception as e_conn: print(f"Error cerrando conexión GET: {e_conn}")

@app.route('/pedidos/eliminar/<int:id>', methods=['POST'])
@login_required
def eliminar_pedido(id):
    conn = get_db_connection()
    if not conn:
        return redirect(url_for('index'))

    cursor = conn.cursor()
    try:
        cursor.execute("DELETE FROM PEDIDOS_WEB WHERE ID = %s AND USUARIO = %s", (id, session['user_id']))
        conn.commit()
        if cursor.rowcount > 0:
            flash("Pedido eliminado exitosamente.", "success")
        else:
            flash("Pedido no encontrado o no tienes permiso para eliminarlo.", "warning")
    except mysql.connector.Error as err:
        conn.rollback()
        flash(f"Error al eliminar pedido: {err}", "danger")
        print(f"ERROR DB Deleting pedido: {err}")
    finally:
        cursor.close()
        conn.close()
    return redirect(url_for('index'))

@app.route('/pedidos/eliminar_completo/<numped_a_eliminar>', methods=['POST'])
@login_required
def eliminar_pedido_completo(numped_a_eliminar):
    """
    Elimina todas las líneas de un pedido (identificado por NUMPED)
    para el usuario actualmente logueado.
    """
    usuario_actual = session['user_id']
    conn = get_db_connection()

    if not conn:
        # flash ya se habrá mostrado
        return redirect(url_for('index'))

    cursor = conn.cursor()
    try:
        # Primero, verificar si el pedido existe y pertenece al usuario (opcional pero recomendado)
        # Esto evita intentos de eliminar pedidos de otros si alguien adivina un NUMPED,
        # aunque la cláusula WHERE en DELETE ya debería protegerlo.
        cursor.execute("SELECT COUNT(*) FROM PEDIDOS_WEB WHERE NUMPED = %s AND USUARIO = %s", 
                       (numped_a_eliminar, usuario_actual))
        count = cursor.fetchone()[0]

        if count == 0:
            flash(f"El pedido {numped_a_eliminar} no se encontró o no tienes permiso para eliminarlo.", "warning")
        else:
            # Eliminar todas las filas (líneas) que coincidan con el NUMPED y el USUARIO
            cursor.execute("DELETE FROM PEDIDOS_WEB WHERE NUMPED = %s AND USUARIO = %s", 
                           (numped_a_eliminar, usuario_actual))
            conn.commit()
            
            if cursor.rowcount > 0:
                flash(f"Pedido {numped_a_eliminar} eliminado.", "success")
            else:
                # Esto podría pasar si la verificación de COUNT falló por alguna razón o hubo una condición de carrera.
                flash(f"No se eliminaron líneas para el pedido {numped_a_eliminar}. Puede que ya estuviera vacío o hubo un problema.", "warning")

    except mysql.connector.Error as err:
        conn.rollback()
        flash(f"Error al eliminar el pedido {numped_a_eliminar}: {err}", "danger")
        print(f"ERROR DB Deleting complete order {numped_a_eliminar}: {err}")
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()
            
    return redirect(url_for('index'))

@app.route('/pedidos/ver/<numped_a_ver>')
@login_required
def ver_pedido(numped_a_ver):
    usuario_actual = session['user_id']
    conn_get = None
    cursor_get = None
    
    try:
        conn_get = get_db_connection()
        if not conn_get:
            return redirect(url_for('index')) # Flash ya se mostró
        
        cursor_get = conn_get.cursor(dictionary=True)
        
        # Obtener todas las líneas del pedido y datos relacionados
        cursor_get.execute("""
            SELECT 
                pw.ID, pw.NUMPED, pw.ARTI, pw.CANTIDAD, pw.COD_CTE, pw.COD_DIR,
                pw.FECHA_CREACION, pw.FECHA_EXP, pw.PEDIDO_CTE, pw.USUARIO, pw.ESTADO, pw.OBSERVACIONES,
                c.RAZON_SOCIAL,
                itm.DESCRIPCION AS DESCRIPCION_ARTI, 
                itm.UD_EMB,
                itm.PRECIO AS PRECIO_ARTI,
                itm.STOCK_FAM 
            FROM PEDIDOS_WEB pw
            LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE
            LEFT JOIN ITMMASTER itm ON pw.ARTI = itm.COD_ART
            WHERE pw.NUMPED = %s AND pw.USUARIO = %s 
            ORDER BY pw.ID ASC
        """, (numped_a_ver, usuario_actual))
        lineas_pedido_db = cursor_get.fetchall()

        if not lineas_pedido_db:
            flash("Pedido no encontrado o no tienes acceso para verlo.", "warning")
            return redirect(url_for('index'))

        # Tomar los datos de cabecera de la primera línea
        cabecera_db = lineas_pedido_db[0]
        pedido_cabecera_para_vista = {
            'NUMPED': cabecera_db['NUMPED'],
            'COD_CTE': cabecera_db.get('COD_CTE'),
            'RAZON_SOCIAL': cabecera_db.get('RAZON_SOCIAL'),
            'COD_DIR': cabecera_db.get('COD_DIR'),
            'FECHA_CREACION': cabecera_db.get('FECHA_CREACION'),
            'FECHA_EXP': cabecera_db.get('FECHA_EXP'),
            'PEDIDO_CTE': cabecera_db.get('PEDIDO_CTE'),
            'USUARIO': cabecera_db.get('USUARIO'),
            'ESTADO': cabecera_db.get('ESTADO', 'Desconocido'),
            'OBSERVACIONES': cabecera_db.get('OBSERVACIONES')
        }

        lineas_para_vista = []
        for linea_db in lineas_pedido_db:
            lineas_para_vista.append({
                'ID_LINEA': linea_db['ID'], 
                'ARTI': linea_db['ARTI'],
                'DESCRIPCION_ARTI': linea_db.get('DESCRIPCION_ARTI', 'N/A'),
                'CANTIDAD': linea_db['CANTIDAD'],
                'PRECIO_ARTI': linea_db.get('PRECIO_ARTI'), # Se pasará como número
                'UD_EMB': linea_db.get('UD_EMB'),
                # STOCK_FAM no es estrictamente necesario para la vista de solo lectura,
                # pero podría ser útil si decides mostrarlo.
            })
        
        # Opcional: Obtener la dirección completa si hay COD_DIR y COD_CTE
        direccion_completa_info = None
        if pedido_cabecera_para_vista.get('COD_CTE') and pedido_cabecera_para_vista.get('COD_DIR'):
            try: # Usar un nuevo cursor o la misma conexión si se maneja bien
                # Es mejor un nuevo cursor si la conexión sigue abierta
                if conn_get.is_connected(): # Reutilizar la conexión si es posible
                    cursor_dir = conn_get.cursor(dictionary=True) # Nuevo cursor
                    cursor_dir.execute("""
                        SELECT DIR, CP, CIUDAD, PROVINCIA, PAIS 
                        FROM DIRECCIONES_CTE_WEB 
                        WHERE COD_CTE = %s AND COD_DIR = %s
                    """, (pedido_cabecera_para_vista['COD_CTE'], pedido_cabecera_para_vista['COD_DIR']))
                    direccion_completa_info = cursor_dir.fetchone()
                    cursor_dir.close() # Cerrar este cursor específico
            except mysql.connector.Error as err_dir:
                print(f"Error obteniendo dirección completa para vista: {err_dir}")


        return render_template('pedido_ver_detalle.html', 
                               pedido_cabecera=pedido_cabecera_para_vista, 
                               lineas_pedido=lineas_para_vista,
                               direccion_completa=direccion_completa_info)

    except mysql.connector.Error as db_err:
        flash(f"Error de base de datos al ver el pedido: {db_err}", "danger")
        print(f"ERROR DB (ver_pedido): {db_err}")
        return redirect(url_for('index'))
    except Exception as e:
        flash(f"Error inesperado al ver el pedido: {e}", "danger")
        print(f"ERROR General (ver_pedido): {type(e).__name__} - {e}")
        return redirect(url_for('index'))
    finally:
        if cursor_get: # Cerrar el cursor principal
            try: cursor_get.close()
            except Exception as e_cursor: print(f"Error cerrando cursor_get (ver_pedido): {e_cursor}")
        if conn_get and conn_get.is_connected(): # Cerrar la conexión principal
            try: conn_get.close()
            except Exception as e_conn: print(f"Error cerrando conn_get (ver_pedido): {e_conn}")


@app.route('/sugerencias_articulo')
@login_required
def sugerencias_articulo():
    query_param = request.args.get('q', '').strip()
    sugerencias_list = []

    # Longitud mínima para la búsqueda de artículos
    # Ajusta el '1' si quieres que busque desde el primer carácter, o '2' para ser más restrictivo.
    min_query_length = 1 
    if len(query_param) < min_query_length:
        return jsonify(sugerencias=[])

    conn = get_db_connection()
    if not conn:
        # El error ya se flasheó en get_db_connection
        return jsonify(error="Error de conexión a la base de datos"), 500

    cursor = conn.cursor(dictionary=True)
    try:
        # Modificamos la consulta para seleccionar también UD_EMB
        sql_query = """
            SELECT COD_ART, DESCRIPCION, UD_EMB, PRECIO, STOCK_FAM
            FROM ITMMASTER 
            WHERE COD_ART LIKE %s OR DESCRIPCION LIKE %s 
            ORDER BY COD_ART 
            LIMIT 10 
        """
        # Usamos '%' al inicio y al final para buscar coincidencias parciales en cualquier parte.
        # Si prefieres que solo busque artículos que COMIENCEN con query_param, usa: query_param + '%'
        search_term = '%' + query_param + '%'
        
        cursor.execute(sql_query, (search_term, search_term))
        resultados = cursor.fetchall()

        for row in resultados:
            sugerencias_list.append({
                'cod_art': row['COD_ART'], 
                'descripcion': row['DESCRIPCION'],
                'ud_emb': row['UD_EMB'],
                'precio': row['PRECIO'],
                'stock_fam': row['STOCK_FAM']
            })

    except mysql.connector.Error as err:
        print(f"Error en la base de datos al obtener sugerencias de artículo: {err}")
        # Es mejor no exponer detalles del error SQL al cliente en producción.
        return jsonify(error="Error al consultar la base de datos"), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

    return jsonify(sugerencias=sugerencias_list)

@app.route('/sugerencias_cliente')
@login_required
def sugerencias_cliente():
    query_param = request.args.get('q', '')
    sugerencias_list = []

    if len(query_param) < 2: # O el mínimo que consideres
        return jsonify(sugerencias=[])

    conn = get_db_connection()
    if not conn:
        return jsonify(error="Error de conexión"), 500

    cursor = conn.cursor(dictionary=True)
    try:
        sql_query = """
            SELECT COD_CTE, RAZON_SOCIAL 
            FROM CLIENTES_WEB 
            WHERE COD_CTE LIKE %s OR RAZON_SOCIAL LIKE %s 
            LIMIT 10
        """
        search_term = '%' + query_param + '%' # Busca en cualquier parte
        cursor.execute(sql_query, (search_term, search_term))
        
        resultados = cursor.fetchall()
        for row in resultados:
            sugerencias_list.append({'cod_cte': row['COD_CTE'], 'razon_social': row['RAZON_SOCIAL']})
    except mysql.connector.Error as err:
        print(f"Error en sugerencias_cliente: {err}")
        return jsonify(error="Error de base de datos"), 500
    finally:
        cursor.close()
        conn.close()
    return jsonify(sugerencias=sugerencias_list)

# En app.py

@app.route('/sugerencias_direccion_cliente')
@login_required
def sugerencias_direccion_cliente():
    query_param = request.args.get('q', '').strip()
    cod_cte_seleccionado = request.args.get('cod_cte', '').strip()
    sugerencias_list = []

    if not cod_cte_seleccionado:
        return jsonify(sugerencias=[], mensaje="Debe seleccionar un cliente válido primero.")
    
    # Si q es '*', listamos todas las direcciones para el cliente
    # Si no, filtramos por q (COD_DIR o DIR)
    
    conn = get_db_connection()
    if not conn:
        return jsonify(error="Error de conexión a BD"), 500

    cursor = conn.cursor(dictionary=True)
    try:
        params = [cod_cte_seleccionado]
        sql_conditions = "COD_CTE = %s"

        if query_param == "*":
            # No se añade más condición, se listan todas para el COD_CTE
            pass
        elif query_param: # Si hay término de búsqueda y no es "*"
            sql_conditions += " AND (COD_DIR LIKE %s OR DIR LIKE %s)"
            search_term = '%' + query_param + '%'
            params.extend([search_term, search_term])
        else: # Si q está vacío y no es "*", no devolver nada o un mensaje
             return jsonify(sugerencias=[], mensaje="Escriba un término de búsqueda o '*' para listar.")


        sql_query = f"""
            SELECT COD_DIR, DIR, CP, CIUDAD, PROVINCIA, PAIS 
            FROM DIRECCIONES_CTE_WEB 
            WHERE {sql_conditions}
            ORDER BY COD_DIR
            LIMIT 600 
        """ # Aumento el LIMIT por si un cliente tiene muchas direcciones
        
        cursor.execute(sql_query, tuple(params))
        resultados = cursor.fetchall()

        for row in resultados:
            sugerencias_list.append({
                'COD_DIR': row['COD_DIR'], 
                'DIR': row['DIR'],
                'CP': row['CP'],
                'CIUDAD': row['CIUDAD'],
                'PROVINCIA': row['PROVINCIA'],
                'PAIS': row['PAIS']
            })
    except mysql.connector.Error as err:
        print(f"Error DB en sugerencias_direccion_cliente: {err}")
        return jsonify(error="Error de base de datos"), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()
    return jsonify(sugerencias=sugerencias_list)

def generar_numped():
    """
    Genera un número de pedido único con formato PED-timestamp_segundos-aleatorio_2digitos.
    La longitud total aproximada es de 17 caracteres.
    """
    #timestamp_segundos = int(time.time())       # Timestamp en segundos (aprox. 10 dígitos)
    now = datetime.datetime.now()
    timestamp_segundos = now.strftime("%y%m%d%H%M")
    aleatorio = random.randint(10, 99)          # Aleatorio de 2 dígitos (10-99)
    
    # Formato: PED-SSSSSSSSSS-RR (S=segundo, R=aleatorio)
    # Longitud: 4  +    10    + 1 +  2  = 17 caracteres
    numped = f"PED-{timestamp_segundos}-{aleatorio:02d}" # :02d asegura 2 dígitos para el aleatorio (ej. 07)
    
    # Verificación de longitud (opcional, pero bueno para desarrollo)
    if len(numped) > 20:
        print(f"ADVERTENCIA: NUMPED generado ('{numped}') excede los 20 caracteres.")
        # Aquí podrías truncar o lanzar un error si es crítico,
        # pero con el cálculo anterior no debería pasar.
        # Para este caso, lo dejaremos pasar si ocurriera, pero el cálculo indica que no.

    return numped

# Ejemplo de uso y prueba de longitud:
if __name__ == '__main__': # Esto solo se ejecuta si corres este script directamente
    for _ in range(5):
        np = generar_numped()
        print(f"NUMPED: {np}, Longitud: {len(np)}")

if __name__ == '__main__':
    #app.run(debug=True) # debug=True solo para desarrollo
    app.run(host='0.0.0.0', port=5000, debug=True)
