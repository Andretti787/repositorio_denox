from flask import Flask, render_template, request, redirect, url_for, session, flash, jsonify, g
import mysql.connector
import time
import random
import datetime
from datetime import timedelta # Importar timedelta
from functools import wraps
from werkzeug.security import check_password_hash # Importar para verificar hashes de contraseñas
import math


app = Flask(__name__)
app.secret_key = 'tu_super_secreto_key_aqui' # ¡Cambia esto en producción!

app.permanent_session_lifetime = timedelta(minutes=45) # Configurar el tiempo de vida de la sesión

# Configuración de la base de datos
db_config = {
    'host': '192.168.35.25',
    'user': 'mmarco',         # Reemplaza con tu usuario
    'password': '@System345', # Reemplaza con tu contraseña
    'database': 'pract'  # El nombre de tu BD
}

@app.before_request
def before_request():
    """Se ejecuta antes de cada petición. Establece la conexión a la BD."""
    session.permanent = True # Refresca la sesión en cada petición
    try:
        g.db = mysql.connector.connect(**db_config)
    except mysql.connector.Error as err:
        g.db = None
        flash(f"Error de conexión a la base de datos: {err}", "danger")
        print(f"ERROR DB Connection: {err}")

@app.teardown_request
def teardown_request(exception=None):
    """Se ejecuta después de cada petición. Cierra la conexión a la BD."""
    db = g.pop('db', None)
    if db is not None and db.is_connected():
        db.close()

# La función get_db_connection() ya no es necesaria y se ha eliminado.
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

        if not g.db: # La conexión falló en before_request
            return render_template('login.html')

        cursor = g.db.cursor(dictionary=True)
        user = None
        try:
            # La columna PWD debería almacenar el hash de la contraseña.
            # Al crear un usuario, se debe usar: generate_password_hash(password)
            # Seleccionamos también la nueva columna TARIFA
            cursor.execute("SELECT COD_USER, NOMBRE, PWD, TARIFA FROM USUARIOS_LOGIN WHERE COD_USER = %s", (username,))
            user = cursor.fetchone()

            # SEGURIDAD: Comparamos el hash de la BD con la contraseña enviada.
            if user and user.get('PWD') and check_password_hash(user['PWD'], password):
                session['user_id'] = user['COD_USER']
                session['user_tarifa'] = user.get('TARIFA', 1) # Guardamos la tarifa en la sesión, con 1 como valor por defecto.
                flash(f"Bienvenid@, {user['NOMBRE']}!", "success")
                next_url = request.args.get('next')
                return redirect(next_url or url_for('index'))

        except mysql.connector.Error as err:
            flash(f"Error al consultar usuario: {err}", "danger")
            print(f"ERROR DB Login query: {err}")
        finally:
            cursor.close()

        # Si la autenticación falla por cualquier motivo, mostramos un mensaje genérico.
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
    if not g.db:
        return render_template('pedidos_lista.html', pedidos_para_mostrar={}) # Pasar dict vacío

    # Capturar filtros de la URL
    filter_cliente = request.args.get('filter_cliente', '').strip()
    filter_direccion = request.args.get('filter_direccion', '').strip()
    page = request.args.get('page', 1, type=int)
    per_page = 10  # Número de pedidos por página

    pedidos_db = []
    clientes_filtro = []
    direcciones_filtro = []

    cursor = g.db.cursor(dictionary=True)
    try:
        # --- Obtener listas para los desplegables de filtro ---
        # Clientes que tienen pedidos
        cursor.execute("""
            SELECT DISTINCT pw.COD_CTE, c.RAZON_SOCIAL 
            FROM PEDIDOS_WEB pw 
            LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE 
            WHERE pw.USUARIO = %s ORDER BY pw.COD_CTE
        """, (session['user_id'],))
        clientes_filtro = cursor.fetchall()

        # Direcciones usadas en pedidos (con su descripción)
        sql_direcciones = """
            SELECT DISTINCT pw.COD_DIR, d.DIR 
            FROM PEDIDOS_WEB pw 
            LEFT JOIN DIRECCIONES_CTE_WEB d ON pw.COD_CTE = d.COD_CTE AND pw.COD_DIR = d.COD_DIR 
            WHERE pw.USUARIO = %s AND pw.COD_DIR IS NOT NULL AND pw.COD_DIR != '' 
        """
        params_direcciones = [session['user_id']]
        if filter_cliente:
            sql_direcciones += " AND pw.COD_CTE = %s"
            params_direcciones.append(filter_cliente)
        sql_direcciones += " ORDER BY pw.COD_DIR"
        cursor.execute(sql_direcciones, tuple(params_direcciones))
        direcciones_filtro = cursor.fetchall()

        # 1. Construir cláusulas WHERE comunes
        where_clauses = ["pw.USUARIO = %s"]
        params = [session['user_id']]

        if filter_cliente:
            where_clauses.append("pw.COD_CTE = %s")
            params.append(filter_cliente)
        
        if filter_direccion:
            where_clauses.append("pw.COD_DIR = %s")
            params.append(filter_direccion)

        where_sql = " AND ".join(where_clauses)

        # 2. Contar total de pedidos distintos (agrupados por NUMPED)
        sql_count = f"SELECT COUNT(DISTINCT pw.NUMPED) as total FROM PEDIDOS_WEB pw LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE WHERE {where_sql}"
        cursor.execute(sql_count, tuple(params))
        total_pedidos = cursor.fetchone()['total']
        total_pages = math.ceil(total_pedidos / per_page)

        # 3. Obtener los NUMPEDs de la página actual (Paginación)
        offset = (page - 1) * per_page
        sql_ids = f"SELECT DISTINCT pw.NUMPED FROM PEDIDOS_WEB pw LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE WHERE {where_sql} ORDER BY pw.NUMPED DESC LIMIT %s OFFSET %s"
        # Añadimos limit y offset a los parámetros
        params_ids = params + [per_page, offset]
        cursor.execute(sql_ids, tuple(params_ids))
        numpeds_result = cursor.fetchall()
        numpeds_page = [row['NUMPED'] for row in numpeds_result]

        # 4. Obtener detalles completos solo para los pedidos de esta página
        if numpeds_page:
            placeholders = ','.join(['%s'] * len(numpeds_page))
            sql_details = f"""
                SELECT pw.ID AS PED_ID, pw.ARTI, pw.CANTIDAD, pw.COD_CTE, pw.COD_DIR, 
                    pw.FECHA_CREACION, pw.FECHA_EXP, pw.PEDIDO_CTE, pw.USUARIO, 
                    pw.NUMPED, pw.ESTADO, pw.DESCUENTO1, pw.DESCUENTO2, pw.MUESTRA,
                    c.RAZON_SOCIAL, itm.DESCRIPCION AS DESCRIPCION_ARTI, itm.UD_EMB,
                    itm.PRECIO AS PRECIO_ARTI, itm.STOCK_FAM
                FROM PEDIDOS_WEB pw
                LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE
                LEFT JOIN ITMMASTER itm ON pw.ARTI = itm.COD_ART
                WHERE pw.USUARIO = %s AND pw.NUMPED IN ({placeholders})
                ORDER BY pw.NUMPED DESC, pw.ID ASC
            """
            params_details = [session['user_id']] + numpeds_page
            cursor.execute(sql_details, tuple(params_details))
            pedidos_db = cursor.fetchall()

    except mysql.connector.Error as err:
        flash(f"Error al obtener pedidos: {err}", "danger")
        print(f"ERROR DB Fetching pedidos: {err}")
    finally:
        cursor.close()

    pedidos_agrupados = {}
    if pedidos_db:
        for linea_pedido in pedidos_db:
            numped = linea_pedido['NUMPED']
            if numped not in pedidos_agrupados:
                pedidos_agrupados[numped] = {
                    'cabecera': {
                        'NUMPED': linea_pedido['NUMPED'],
                        'COD_CTE': linea_pedido['COD_CTE'],
                        'RAZON_SOCIAL': linea_pedido.get('RAZON_SOCIAL') or '', # Usar or '' para manejar None si el cliente se borró
                        'COD_DIR': linea_pedido['COD_DIR'],
                        'FECHA_CREACION': linea_pedido['FECHA_CREACION'],
                        'FECHA_EXP': linea_pedido['FECHA_EXP'],
                        'PEDIDO_CTE': linea_pedido['PEDIDO_CTE'],
                        'USUARIO': linea_pedido['USUARIO'],
                        'ESTADO': linea_pedido.get('ESTADO', 'Desconocido'),
                        'DESCUENTO1': linea_pedido.get('DESCUENTO1'),
                        'MUESTRA': linea_pedido.get('MUESTRA')
                    },
                    'lineas': []
                }
            pedidos_agrupados[numped]['lineas'].append({
                'ID_LINEA': linea_pedido['PED_ID'],
                'ARTI': linea_pedido['ARTI'],
                'DESCRIPCION_ARTI': linea_pedido.get('DESCRIPCION_ARTI') or '',
                'CANTIDAD': linea_pedido['CANTIDAD'],
                'PRECIO_ARTI': linea_pedido.get('PRECIO_ARTI'),
                'UD_EMB': linea_pedido.get('UD_EMB'),
                'STOCK_FAM_ARTI': linea_pedido.get('STOCK_FAM'),
                'DESCUENTO2': linea_pedido.get('DESCUENTO2')
            })

    return render_template('pedidos_lista.html', 
                           pedidos_para_mostrar=pedidos_agrupados,
                           filter_cliente=filter_cliente,
                           filter_direccion=filter_direccion,
                           page=page,
                           total_pages=total_pages,
                           clientes_filtro=clientes_filtro,
                           direcciones_filtro=direcciones_filtro)


def _validar_y_procesar_datos_pedido(form_data, es_muestra, estado_pedido, numped_a_excluir=None):
    """
    Función auxiliar para validar y procesar los datos de un formulario de pedido.
    Reutilizada por agregar_pedido y editar_pedido.
    Devuelve una tupla: (es_valido, datos_procesados, datos_para_repoblar)
    """
    usuario_actual = session['user_id']
    if not g.db:
        flash("Error de conexión a la base de datos.", "danger")
        return False, None, {}

    # --- 1. RECOGER Y PARSEAR DATOS ---
    cod_cte = form_data.get('COD_CTE', '').strip()
    cod_dir = form_data.get('COD_DIR', '').strip()
    fecha_exp_str = form_data.get('FECHA_EXP', '').strip()
    pedido_cte_ref = form_data.get('PEDIDO_CTE', '').strip()
    observaciones = form_data.get('OBSERVACIONES', '').strip()
    descuento1_str = form_data.get('DESCUENTO1', '0').strip()

    # Parsear líneas para repoblar en caso de error
    parsed_lineas_form = {}
    for key, value in form_data.items():
        if key.startswith('lineas['):
            parts = key.replace(']', '').split('[')
            try:
                idx, field_name = int(parts[1]), parts[2]
                if idx not in parsed_lineas_form: parsed_lineas_form[idx] = {}
                parsed_lineas_form[idx][field_name] = value
            except (IndexError, ValueError): continue
    
    lineas_para_repoblar = [parsed_lineas_form[idx] for idx in sorted(parsed_lineas_form.keys()) if 'ARTI' in parsed_lineas_form[idx] or 'CANTIDAD' in parsed_lineas_form[idx]]

    cabecera_para_repoblar = {
        'COD_CTE': cod_cte, 'COD_DIR': cod_dir, 'FECHA_EXP': fecha_exp_str,
        'PEDIDO_CTE': pedido_cte_ref, 'OBSERVACIONES': observaciones, 'DESCUENTO1': descuento1_str
    }
    datos_para_repoblar = {'pedido_cabecera': cabecera_para_repoblar, 'lineas_pedido': lineas_para_repoblar}

    # --- 2. VALIDACIONES DE CABECERA ---
    if len(observaciones) > 100:
        flash("El campo Observaciones no puede tener más de 100 caracteres.", "danger")
        return False, None, datos_para_repoblar

    try:
        descuento1_val = int(descuento1_str) if descuento1_str else 0
        if not 0 <= descuento1_val <= 100: raise ValueError()
    except ValueError:
        flash(f"Descuento General ('{descuento1_str}') inválido. Debe ser un número entero entre 0 y 100.", "danger")
        return False, None, datos_para_repoblar

    if not cod_cte:
        flash("El Código de Cliente es obligatorio.", "danger")
        return False, None, datos_para_repoblar

    cursor = g.db.cursor(dictionary=True)
    tarifa_usuario = session.get('user_tarifa', 1) # Obtenemos la tarifa del usuario desde la sesión
    try:
        # 1. Validar que el cliente es visible para el usuario
        sql_val_cte = """
            SELECT c.COD_CTE FROM CLIENTES_WEB c
            LEFT JOIN DIRECCIONES_CTE_WEB d ON c.COD_CTE = d.COD_CTE
            WHERE c.COD_CTE = %s AND (c.COD_USER = %s OR d.COD_USER = %s)
            LIMIT 1
        """
        cursor.execute(sql_val_cte, (cod_cte, usuario_actual, usuario_actual))
        if not cursor.fetchone():
            flash(f"El Código de Cliente '{cod_cte}' no existe o no tiene permiso para usarlo.", "danger")
            return False, None, datos_para_repoblar

        if cod_dir:
            # 2. Validar que la dirección es visible para el usuario
            sql_val_dir = """
                SELECT d.COD_DIR 
                FROM DIRECCIONES_CTE_WEB d 
                JOIN CLIENTES_WEB c ON d.COD_CTE = c.COD_CTE
                WHERE d.COD_CTE = %s AND d.COD_DIR = %s 
                AND (d.COD_USER = %s OR d.COD_USER IS NULL OR d.COD_USER = '' OR c.COD_USER = %s)
            """
            cursor.execute(sql_val_dir, (cod_cte, cod_dir, usuario_actual, usuario_actual))
            if not cursor.fetchone():
                flash(f"La Dirección '{cod_dir}' no es válida para el cliente '{cod_cte}' o no tiene permiso para usarla.", "danger")
                return False, None, datos_para_repoblar

        # NUEVA VALIDACIÓN: Comprobar si la combinación COD_CTE y PEDIDO_CTE ya existe
        if pedido_cte_ref:
            sql_check_duplicado = "SELECT COUNT(*) as count FROM PEDIDOS_WEB WHERE COD_CTE = %s AND PEDIDO_CTE = %s AND USUARIO = %s"
            params_check = [cod_cte, pedido_cte_ref, usuario_actual]

            if numped_a_excluir:
                sql_check_duplicado += " AND NUMPED != %s"
                params_check.append(numped_a_excluir)

            cursor.execute(sql_check_duplicado, tuple(params_check))
            if cursor.fetchone()['count'] > 0:
                flash(f"Ya existe un pedido con la referencia '{pedido_cte_ref}' para el cliente '{cod_cte}'.", "danger")
                return False, None, datos_para_repoblar


    except mysql.connector.Error as err:
        flash(f"Error al validar cabecera: {err}", "danger")
        return False, None, datos_para_repoblar
    finally:
        cursor.close()

    if not fecha_exp_str:
        flash("La Fecha de Entrega es obligatoria.", "danger")
        return False, None, datos_para_repoblar
    try:
        fecha_exp_obj = datetime.datetime.strptime(fecha_exp_str, '%Y-%m-%d').date()
        if fecha_exp_obj <= datetime.date.today():
            flash("La Fecha de Entrega debe ser posterior a la fecha actual.", "danger")
            return False, None, datos_para_repoblar
    except ValueError:
        flash("Formato de Fecha de Entrega inválido. Usar YYYY-MM-DD.", "danger")
        return False, None, datos_para_repoblar

    # --- 3. VALIDACIONES DE LÍNEAS ---
    if not lineas_para_repoblar:
        flash("Debe agregar al menos una línea al pedido.", "danger")
        return False, None, datos_para_repoblar

    lineas_procesadas_ok = []
    for idx, linea_data in enumerate(lineas_para_repoblar):
        arti_linea = linea_data.get('ARTI', '').strip()
        cantidad_str = linea_data.get('CANTIDAD', '').strip()
        descuento2_str = linea_data.get('DESCUENTO2', '0').strip()

        if not arti_linea:
            flash(f"El Artículo es obligatorio en la línea {idx + 1}.", "danger")
            return False, None, datos_para_repoblar

        cursor = g.db.cursor(dictionary=True)
        ud_emb_articulo = None
        try:
            # Recuperamos también el PRECIO para guardarlo
            # Modificamos la consulta para traer los 4 precios
            cursor.execute("SELECT UD_EMB, PRECIO, PRECIO_2, PRECIO_3, PRECIO_4 FROM ITMMASTER WHERE COD_ART = %s", (arti_linea,))
            item_maestro = cursor.fetchone()
            if not item_maestro:
                flash(f"Artículo '{arti_linea}' inválido en línea {idx + 1}.", "danger")
                return False, None, datos_para_repoblar
            
            # Lógica para seleccionar el precio correcto según la tarifa
            precio_a_usar = item_maestro.get('PRECIO', 0.0) # Precio por defecto (Tarifa 1)
            if tarifa_usuario == 2 and item_maestro.get('PRECIO_2', 0.0) > 0:
                precio_a_usar = item_maestro.get('PRECIO_2')
            elif tarifa_usuario == 3 and item_maestro.get('PRECIO_3', 0.0) > 0:
                precio_a_usar = item_maestro.get('PRECIO_3')
            elif tarifa_usuario == 4 and item_maestro.get('PRECIO_4', 0.0) > 0:
                precio_a_usar = item_maestro.get('PRECIO_4')


            raw_ud_emb = item_maestro.get('UD_EMB')
            if raw_ud_emb is not None:
                try:
                    temp_ud_emb = int(float(raw_ud_emb))
                    if temp_ud_emb > 0: ud_emb_articulo = temp_ud_emb
                except (ValueError, TypeError): pass
        except mysql.connector.Error as err:
            flash(f"Error al validar artículo en línea {idx + 1}: {err}", "danger")
            return False, None, datos_para_repoblar
        finally:
            cursor.close()

        if not cantidad_str:
            flash(f"La Cantidad es obligatoria en la línea {idx + 1}.", "danger")
            return False, None, datos_para_repoblar
        try:
            cantidad_linea = int(cantidad_str)
            if cantidad_linea <= 0: raise ValueError("La cantidad debe ser positiva.")
            if es_muestra != "X" and ud_emb_articulo and (cantidad_linea % ud_emb_articulo != 0):
                flash(f"La cantidad ({cantidad_linea}) para '{arti_linea}' (línea {idx + 1}) debe ser múltiplo de {ud_emb_articulo}.", "danger")
                return False, None, datos_para_repoblar
        except ValueError as e:
            flash(f"Cantidad inválida ('{cantidad_str}') en línea {idx + 1}: {e}", "danger")
            return False, None, datos_para_repoblar

        try:
            descuento2_val = int(descuento2_str) if descuento2_str else 0
            if not 0 <= descuento2_val <= 100: raise ValueError()
        except ValueError:
            flash(f"Descuento de Línea ('{descuento2_str}') inválido en línea {idx + 1}.", "danger")
            return False, None, datos_para_repoblar

        # Determinar precio a guardar: 0 si es muestra, sino el que corresponde por tarifa
        precio_guardar = 0.0
        if es_muestra != 'X':
            precio_guardar = float(precio_a_usar)

        lineas_procesadas_ok.append({'ARTI': arti_linea, 'CANTIDAD': cantidad_linea, 'DESCUENTO2': descuento2_val, 'PRECIO': precio_guardar})

    if not lineas_procesadas_ok:
        flash("No se procesó ninguna línea válida.", "danger")
        return False, None, datos_para_repoblar

    # --- 4. DEVOLVER DATOS PROCESADOS ---
    datos_procesados = {
        'cod_cte': cod_cte,
        'cod_dir': cod_dir,
        'fecha_exp_obj': fecha_exp_obj,
        'pedido_cte_ref': pedido_cte_ref,
        'observaciones': observaciones,
        'descuento1_val': descuento1_val,
        'usuario_actual': usuario_actual,
        'estado_pedido': estado_pedido,
        'es_muestra': es_muestra,
        'lineas': lineas_procesadas_ok
    }

    return True, datos_procesados, datos_para_repoblar

@app.route('/pedidos/nuevo', methods=['GET', 'POST'])
@login_required
def agregar_pedido():
    if request.method == 'POST':
        if not g.db:
            return render_template('pedido_form.html', pedido_cabecera={}, lineas_pedido=[])

        estado_pedido = "BORRADOR" if 'borrador' in request.form else "PENDIENTE"
        
        if 'proforma' in request.form:
            es_muestra = "P"
        elif 'muestra' in request.form:
            es_muestra = "X"
        else:
            es_muestra = ""

        es_valido, datos_procesados, datos_para_repoblar = _validar_y_procesar_datos_pedido(request.form, es_muestra, estado_pedido)

        if not es_valido:
            return render_template('pedido_form.html', **datos_para_repoblar)

        # --- SI TODAS LAS VALIDACIONES PASAN, GENERAR NUMPED E INSERTAR ---
        numped_nuevo = generar_numped()
        fecha_creacion_actual = datetime.datetime.now() 

        cursor = g.db.cursor()
        sql_insert_linea = """
            INSERT INTO PEDIDOS_WEB (NUMPED, ARTI, CANTIDAD, COD_CTE, COD_DIR,
                                     FECHA_CREACION, FECHA_EXP, PEDIDO_CTE, USUARIO, ESTADO, OBSERVACIONES, DESCUENTO1, DESCUENTO2, MUESTRA, PRECIO)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        try:
            for linea in datos_procesados['lineas']:
                val = (
                    numped_nuevo,
                    linea['ARTI'],
                    linea['CANTIDAD'],
                    datos_procesados['cod_cte'] or None,
                    datos_procesados['cod_dir'] or None,
                    fecha_creacion_actual, 
                    datos_procesados['fecha_exp_obj'], 
                    datos_procesados['pedido_cte_ref'] or None,
                    datos_procesados['usuario_actual'],
                    datos_procesados['estado_pedido'],
                    datos_procesados['observaciones'] or None,
                    datos_procesados['descuento1_val'],
                    linea['DESCUENTO2'],
                    datos_procesados['es_muestra'],
                    linea['PRECIO']
                )
                cursor.execute(sql_insert_linea, val)
            
            g.db.commit()
            flash(f"Pedido {numped_nuevo} creado exitosamente con {len(datos_procesados['lineas'])} líneas.", "success")
            return redirect(url_for('index'))

        except mysql.connector.Error as err:
            g.db.rollback()
            flash(f"Error al guardar el pedido en la base de datos: {err}", "danger")
            print(f"ERROR DB creating multi-line order: {err}")
            return render_template('pedido_form.html', **datos_para_repoblar)
        finally:
            cursor.close()

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
        # Inicializar datos_para_repoblar con los datos del formulario para asegurar que siempre esté definido
        # en caso de un error inesperado antes de que _validar_y_procesar_datos_pedido retorne.
        cod_cte_initial = request.form.get('COD_CTE', '').strip()
        cod_dir_initial = request.form.get('COD_DIR', '').strip()
        fecha_exp_str_initial = request.form.get('FECHA_EXP', '').strip()
        pedido_cte_ref_initial = request.form.get('PEDIDO_CTE', '').strip()
        observaciones_initial = request.form.get('OBSERVACIONES', '').strip()
        descuento1_str_initial = request.form.get('DESCUENTO1', '0').strip()

        parsed_lineas_form_initial = {}
        for key, value in request.form.items():
            if key.startswith('lineas['):
                parts = key.replace(']', '').split('[')
                try:
                    idx, field_name = int(parts[1]), parts[2]
                    if idx not in parsed_lineas_form_initial: parsed_lineas_form_initial[idx] = {}
                    parsed_lineas_form_initial[idx][field_name] = value
                except (IndexError, ValueError): continue
        
        lineas_para_repoblar_initial = [parsed_lineas_form_initial[idx] for idx in sorted(parsed_lineas_form_initial.keys()) if 'ARTI' in parsed_lineas_form_initial[idx] or 'CANTIDAD' in parsed_lineas_form_initial[idx]]

        datos_para_repoblar = {
            'pedido_cabecera': {'NUMPED': numped_a_editar, 'COD_CTE': cod_cte_initial, 'COD_DIR': cod_dir_initial, 'FECHA_EXP': fecha_exp_str_initial, 'PEDIDO_CTE': pedido_cte_ref_initial, 'OBSERVACIONES': observaciones_initial, 'DESCUENTO1': descuento1_str_initial},
            'lineas_pedido': lineas_para_repoblar_initial
        }

        if not g.db:
            # Si la conexión falla, intentamos repoblar el formulario con los datos que venían
            # Esto es un fallback, ya que la validación no se podrá completar.
            return render_template('pedido_form.html', pedido_cabecera={'NUMPED': numped_a_editar}, lineas_pedido=[])

        try:
            estado_pedido = "BORRADOR" if 'borrador' in request.form else "PENDIENTE"
            
            if 'proforma' in request.form:
                es_muestra = "P"
            elif 'muestra' in request.form:
                es_muestra = "X"
            else:
                es_muestra = ""

            # Esta llamada reasignará datos_para_repoblar si se ejecuta con éxito
            es_valido, datos_procesados, datos_para_repoblar = _validar_y_procesar_datos_pedido(request.form, es_muestra, estado_pedido, numped_a_editar) 

            if not es_valido:
                datos_para_repoblar['pedido_cabecera']['NUMPED'] = numped_a_editar
                return render_template('pedido_form.html', **datos_para_repoblar)

            # --- SI TODAS LAS VALIDACIONES PASAN, ACTUALIZAR EN BD ---
            cursor = g.db.cursor(buffered=True, dictionary=True)
            
            cursor.execute("SELECT USUARIO, FECHA_CREACION FROM PEDIDOS_WEB WHERE NUMPED = %s AND USUARIO = %s LIMIT 1", 
                               (numped_a_editar, usuario_actual))
            pedido_original = cursor.fetchone() 
            if not pedido_original:
                flash("Pedido a editar no encontrado o sin permiso.", "warning")
                return redirect(url_for('index')) 
            
            fecha_creacion_original = pedido_original['FECHA_CREACION']
            cursor.execute("DELETE FROM PEDIDOS_WEB WHERE NUMPED = %s AND USUARIO = %s", (numped_a_editar, usuario_actual))

            sql_reinsert = "INSERT INTO PEDIDOS_WEB (NUMPED, ARTI, CANTIDAD, COD_CTE, COD_DIR, FECHA_CREACION, FECHA_EXP, PEDIDO_CTE, USUARIO, ESTADO, OBSERVACIONES, DESCUENTO1, DESCUENTO2, MUESTRA, PRECIO) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
            for linea in datos_procesados['lineas']:
                val_reinsert = (
                    numped_a_editar, linea['ARTI'], linea['CANTIDAD'],
                    datos_procesados['cod_cte'] or None, datos_procesados['cod_dir'] or None, fecha_creacion_original,
                    datos_procesados['fecha_exp_obj'], datos_procesados['pedido_cte_ref'] or None, usuario_actual, estado_pedido,
                    datos_procesados['observaciones'] or None, datos_procesados['descuento1_val'], linea['DESCUENTO2'], es_muestra, linea['PRECIO']
                )
                cursor.execute(sql_reinsert, val_reinsert)
            
            g.db.commit()
            flash(f"Pedido {numped_a_editar} actualizado.", "success")
            return redirect(url_for('index'))

        except mysql.connector.Error as db_err:
            if g.db and g.db.is_connected(): g.db.rollback()
            flash(f"Error de BD al editar el pedido: {db_err}", "danger")
            print(f"ERROR DB (POST editar_pedido): {db_err}")
            # La función de validación ya preparó los datos para repoblar
            return render_template('pedido_form.html', **datos_para_repoblar)
        except Exception as e:
            if g.db and g.db.is_connected(): g.db.rollback()
            flash(f"Error inesperado al editar el pedido: {e}", "danger")
            print(f"ERROR General (POST editar_pedido): {type(e).__name__} - {e}")
            return render_template('pedido_form.html', **datos_para_repoblar)
        finally:
            if 'cursor' in locals() and cursor: cursor.close()

    # --- MÉTODO GET: Cargar datos para el formulario de edición ---
    else: 
        try:
            if not g.db: return redirect(url_for('index'))
            cursor = g.db.cursor(dictionary=True)
            
            # Modificamos la consulta para incluir DESCRIPCION y UD_EMB de ITMMASTER
            cursor.execute("""
                SELECT pw.ID, pw.NUMPED, pw.ARTI, pw.CANTIDAD, pw.COD_CTE, pw.COD_DIR, pw.DESCUENTO1, pw.DESCUENTO2,
                       pw.FECHA_CREACION, pw.FECHA_EXP, pw.PEDIDO_CTE, pw.USUARIO, pw.ESTADO, pw.OBSERVACIONES, pw.MUESTRA,
                       itm.DESCRIPCION AS DESCRIPCION_ARTI, 
                       itm.UD_EMB,
                       COALESCE(pw.PRECIO, itm.PRECIO) AS PRECIO_ARTI,
                       itm.STOCK_FAM
                FROM PEDIDOS_WEB pw
                LEFT JOIN ITMMASTER itm ON pw.ARTI = itm.COD_ART
                WHERE pw.NUMPED = %s AND pw.USUARIO = %s 
                ORDER BY pw.ID ASC
            """, (numped_a_editar, usuario_actual))
            lineas_pedido_db_get = cursor.fetchall()

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
                'OBSERVACIONES': cabecera_db_get.get('OBSERVACIONES') or '',
                'DESCUENTO1': cabecera_db_get.get('DESCUENTO1') or 0,
                'MUESTRA': cabecera_db_get.get('MUESTRA')
            }
            lineas_para_form_get = []
            for linea_db_get in lineas_pedido_db_get:
                lineas_para_form_get.append({
                    'ID_LINEA': linea_db_get['ID'], 
                    'ARTI': linea_db_get['ARTI'],
                    'CANTIDAD': str(linea_db_get['CANTIDAD']),
                    'DESCRIPCION_ARTI': linea_db_get.get('DESCRIPCION_ARTI') or '', # Usar or '' para manejar None
                    'UD_EMB': linea_db_get.get('UD_EMB'), # Usar .get() por si el JOIN no trae UD_EMB
                    'PRECIO_ARTI': linea_db_get.get('PRECIO_ARTI'),
                    'STOCK_FAM_ARTI': linea_db_get.get('STOCK_FAM'),
                    'DESCUENTO2': linea_db_get.get('DESCUENTO2') or 0
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
            if 'cursor' in locals() and cursor: cursor.close()

@app.route('/pedidos/eliminar/<int:id>', methods=['POST'])
@login_required
def eliminar_pedido(id):
    if not g.db:
        return redirect(url_for('index'))

    cursor = g.db.cursor()
    try:
        cursor.execute("DELETE FROM PEDIDOS_WEB WHERE ID = %s AND USUARIO = %s", (id, session['user_id']))
        g.db.commit()
        if cursor.rowcount > 0:
            flash("Pedido eliminado exitosamente.", "success")
        else:
            flash("Pedido no encontrado o no tienes permiso para eliminarlo.", "warning")
    except mysql.connector.Error as err:
        g.db.rollback()
        flash(f"Error al eliminar pedido: {err}", "danger")
        print(f"ERROR DB Deleting pedido: {err}")
    finally:
        cursor.close()
    return redirect(url_for('index'))

@app.route('/pedidos/eliminar_completo/<numped_a_eliminar>', methods=['POST'])
@login_required
def eliminar_pedido_completo(numped_a_eliminar):
    """
    Elimina todas las líneas de un pedido (identificado por NUMPED)
    para el usuario actualmente logueado.
    """
    usuario_actual = session['user_id']
    if not g.db:
        # flash ya se habrá mostrado
        return redirect(url_for('index'))

    cursor = g.db.cursor()
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
            g.db.commit()
            
            if cursor.rowcount > 0:
                flash(f"Pedido {numped_a_eliminar} eliminado.", "success")
            else:
                # Esto podría pasar si la verificación de COUNT falló por alguna razón o hubo una condición de carrera.
                flash(f"No se eliminaron líneas para el pedido {numped_a_eliminar}. Puede que ya estuviera vacío o hubo un problema.", "warning")

    except mysql.connector.Error as err:
        g.db.rollback()
        flash(f"Error al eliminar el pedido {numped_a_eliminar}: {err}", "danger")
        print(f"ERROR DB Deleting complete order {numped_a_eliminar}: {err}")
    finally:
        cursor.close()
            
    return redirect(url_for('index'))

@app.route('/pedidos/ver/<numped_a_ver>')
@login_required
def ver_pedido(numped_a_ver):
    usuario_actual = session['user_id']
    
    try:
        if not g.db:
            return redirect(url_for('index')) # Flash ya se mostró
        
        cursor = g.db.cursor(dictionary=True)
        
        # Obtener todas las líneas del pedido y datos relacionados
        cursor.execute("""
            SELECT 
                pw.ID, pw.NUMPED, pw.ARTI, pw.CANTIDAD, pw.COD_CTE, pw.COD_DIR, pw.DESCUENTO1, pw.DESCUENTO2,
                pw.FECHA_CREACION, pw.FECHA_EXP, pw.PEDIDO_CTE, pw.USUARIO, pw.ESTADO, pw.OBSERVACIONES, pw.MUESTRA,
                c.RAZON_SOCIAL,
                itm.DESCRIPCION AS DESCRIPCION_ARTI, 
                itm.UD_EMB,
                COALESCE(pw.PRECIO, itm.PRECIO) AS PRECIO_ARTI,
                itm.STOCK_FAM 
            FROM PEDIDOS_WEB pw
            LEFT JOIN CLIENTES_WEB c ON pw.COD_CTE = c.COD_CTE
            LEFT JOIN ITMMASTER itm ON pw.ARTI = itm.COD_ART
            WHERE pw.NUMPED = %s AND pw.USUARIO = %s 
            ORDER BY pw.ID ASC
        """, (numped_a_ver, usuario_actual))
        lineas_pedido_db = cursor.fetchall()

        if not lineas_pedido_db:
            flash("Pedido no encontrado o no tienes acceso para verlo.", "warning")
            return redirect(url_for('index'))

        # Tomar los datos de cabecera de la primera línea
        cabecera_db = lineas_pedido_db[0]
        pedido_cabecera_para_vista = {
            'NUMPED': cabecera_db['NUMPED'],
            'COD_CTE': cabecera_db.get('COD_CTE'),
            'RAZON_SOCIAL': cabecera_db.get('RAZON_SOCIAL') or '',
            'COD_DIR': cabecera_db.get('COD_DIR'),
            'FECHA_CREACION': cabecera_db.get('FECHA_CREACION'),
            'FECHA_EXP': cabecera_db.get('FECHA_EXP'),
            'PEDIDO_CTE': cabecera_db.get('PEDIDO_CTE'),
            'USUARIO': cabecera_db.get('USUARIO'),
            'ESTADO': cabecera_db.get('ESTADO', 'Desconocido'),
            'OBSERVACIONES': cabecera_db.get('OBSERVACIONES'),
            'DESCUENTO1': cabecera_db.get('DESCUENTO1'),
            'MUESTRA': cabecera_db.get('MUESTRA')
        }

        lineas_para_vista = []
        for linea_db in lineas_pedido_db:
            lineas_para_vista.append({
                'ID_LINEA': linea_db['ID'], 
                'ARTI': linea_db['ARTI'],
                'DESCRIPCION_ARTI': linea_db.get('DESCRIPCION_ARTI') or 'N/A',
                'CANTIDAD': linea_db['CANTIDAD'],
                'PRECIO_ARTI': linea_db.get('PRECIO_ARTI'), # Se pasará como número
                'UD_EMB': linea_db.get('UD_EMB'),
                'DESCUENTO2': linea_db.get('DESCUENTO2'),
                # STOCK_FAM no es estrictamente necesario para la vista de solo lectura,
                # pero podría ser útil si decides mostrarlo.
            })
        
        # Opcional: Obtener la dirección completa si hay COD_DIR y COD_CTE
        direccion_completa_info = None
        if pedido_cabecera_para_vista.get('COD_CTE') and pedido_cabecera_para_vista.get('COD_DIR'):
            try: # Usar un nuevo cursor o la misma conexión si se maneja bien
                # Es mejor un nuevo cursor si la conexión sigue abierta
                if g.db.is_connected(): # Reutilizar la conexión si es posible
                    cursor_dir = g.db.cursor(dictionary=True) # Nuevo cursor
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
        if 'cursor' in locals() and cursor: cursor.close()
        # La conexión se cierra automáticamente en teardown_request


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

    if not g.db:
        # El error ya se flasheó en get_db_connection
        return jsonify(error="Error de conexión a la base de datos."), 500

    cursor = g.db.cursor(dictionary=True)
    tarifa_usuario = session.get('user_tarifa', 1) # Obtenemos la tarifa del usuario
    try:
        # Modificamos la consulta para seleccionar el precio correcto con un CASE
        sql_query = """
            SELECT 
                COD_ART, DESCRIPCION, UD_EMB, STOCK_FAM,
                CASE
                    WHEN %s = 4 AND PRECIO_4 > 0 THEN PRECIO_4
                    WHEN %s = 3 AND PRECIO_3 > 0 THEN PRECIO_3
                    WHEN %s = 2 AND PRECIO_2 > 0 THEN PRECIO_2
                    ELSE PRECIO
                END AS precio_aplicado
            FROM ITMMASTER 
            WHERE COD_ART LIKE %s OR DESCRIPCION LIKE %s 
            ORDER BY COD_ART 
            LIMIT 40 
        """
        search_term = '%' + query_param + '%'
        
        # Pasamos la tarifa del usuario para cada WHEN en el CASE, y luego los términos de búsqueda
        params = (tarifa_usuario, tarifa_usuario, tarifa_usuario, search_term, search_term)
        cursor.execute(sql_query, params)
        resultados = cursor.fetchall()

        for row in resultados:
            sugerencias_list.append({
                'cod_art': row['COD_ART'], 
                'descripcion': row['DESCRIPCION'],
                'ud_emb': row['UD_EMB'],
                'precio': row['precio_aplicado'], # Usamos el nuevo campo calculado 'precio_aplicado'
                'stock_fam': row['STOCK_FAM']
            })

    except mysql.connector.Error as err:
        print(f"Error en la base de datos al obtener sugerencias de artículo: {err}")
        # Es mejor no exponer detalles del error SQL al cliente en producción.
        return jsonify(error="Error al consultar la base de datos"), 500
    finally:
        cursor.close()

    return jsonify(sugerencias=sugerencias_list)

@app.route('/sugerencias_cliente')
@login_required
def sugerencias_cliente():
    query_param = request.args.get('q', '')
    usuario_actual = session['user_id']
    sugerencias_list = []

    if len(query_param) < 2: # O el mínimo que consideres
        return jsonify(sugerencias=[])

    if not g.db:
        return jsonify(error="Error de conexión a la base de datos."), 500

    cursor = g.db.cursor(dictionary=True)
    try:
        sql_query = """
            SELECT DISTINCT c.COD_CTE, c.RAZON_SOCIAL, c.FPAGO
            FROM CLIENTES_WEB c
            LEFT JOIN DIRECCIONES_CTE_WEB d ON c.COD_CTE = d.COD_CTE
            WHERE
                (c.COD_CTE LIKE %s OR c.RAZON_SOCIAL LIKE %s)
                AND (c.COD_USER = %s OR d.COD_USER = %s)
            LIMIT 10
        """
        search_term = '%' + query_param + '%' # Busca en cualquier parte
        params = (search_term, search_term, usuario_actual, usuario_actual)
        cursor.execute(sql_query, params)
        
        resultados = cursor.fetchall()
        for row in resultados:
            sugerencias_list.append({'cod_cte': row['COD_CTE'], 'razon_social': row['RAZON_SOCIAL'], 'fpago': row['FPAGO']})
    except mysql.connector.Error as err:
        print(f"Error en sugerencias_cliente: {err}")
        return jsonify(error="Error de base de datos"), 500
    finally:
        cursor.close()
    return jsonify(sugerencias=sugerencias_list)

# En app.py

@app.route('/sugerencias_direccion_cliente')
@login_required
def sugerencias_direccion_cliente():
    query_param = request.args.get('q', '').strip()
    cod_cte_seleccionado = request.args.get('cod_cte', '').strip()
    usuario_actual = session['user_id']
    sugerencias_list = []

    if not cod_cte_seleccionado:
        return jsonify(sugerencias=[], mensaje="Debe seleccionar un cliente válido primero.")
    
    # Si q es '*', listamos todas las direcciones para el cliente
    # Si no, filtramos por q (COD_DIR o DIR)
    
    if not g.db:
        return jsonify(error="Error de conexión a la base de datos."), 500

    cursor = g.db.cursor(dictionary=True)
    try:
        # Lógica base: direcciones del cliente que son del usuario o públicas, O si el cliente pertenece al usuario
        params = [cod_cte_seleccionado, usuario_actual, usuario_actual]
        sql_conditions = "d.COD_CTE = %s AND (d.COD_USER = %s OR d.COD_USER IS NULL OR d.COD_USER = '' OR c.COD_USER = %s)"

        # Añadir filtro de búsqueda si existe
        if query_param and query_param != "*":
            sql_conditions += " AND (d.COD_DIR LIKE %s OR d.DIR LIKE %s OR d.CP LIKE %s OR d.CIUDAD LIKE %s OR d.PROVINCIA LIKE %s)"
            search_term = '%' + query_param + '%'
            params.extend([search_term, search_term, search_term, search_term, search_term])


        sql_query = f"""
            SELECT d.COD_DIR, d.DIR, d.CP, d.CIUDAD, d.PROVINCIA, d.PAIS, d.DEFECTO 
            FROM DIRECCIONES_CTE_WEB d
            JOIN CLIENTES_WEB c ON d.COD_CTE = c.COD_CTE
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
                'PAIS': row['PAIS'],
                'DEFECTO': row['DEFECTO']
            })
    except mysql.connector.Error as err:
        print(f"Error DB en sugerencias_direccion_cliente: {err}")
        return jsonify(error="Error de base de datos"), 500
    finally:
        cursor.close()
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
    print("Ejemplos de NUMPED generados:")
    for _ in range(5):
        np = generar_numped()
        print(f"  - NUMPED: {np}, Longitud: {len(np)}")
    
    #app.run(debug=True) # debug=True solo para desarrollo
    app.run(host='0.0.0.0', port=5000, debug=True)
