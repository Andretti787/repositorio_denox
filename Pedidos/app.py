from flask import Flask, render_template, request, redirect, url_for, session, flash, jsonify
import mysql.connector
from functools import wraps
import datetime # Para manejar fechas

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
            flash(f"Bienvenido, {user['COD_USER']}!", "success")
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
    """Muestra la lista de pedidos del usuario logueado."""
    conn = get_db_connection()
    if not conn:
        return render_template('pedidos_lista.html', pedidos=[])

    cursor = conn.cursor(dictionary=True)
    pedidos = []
    try:
        cursor.execute("SELECT * FROM PEDIDOS_WEB WHERE USUARIO = %s ORDER BY FECHA_CREACION DESC", (session['user_id'],))
        pedidos = cursor.fetchall()
    except mysql.connector.Error as err:
        flash(f"Error al obtener pedidos: {err}", "danger")
        print(f"ERROR DB Fetching pedidos: {err}")
    finally:
        cursor.close()
        conn.close()
    return render_template('pedidos_lista.html', pedidos=pedidos)

@app.route('/pedidos/nuevo', methods=['GET', 'POST'])
@login_required
def agregar_pedido():
    if request.method == 'POST':
        # Recoger datos del formulario
        # request.form devuelve un ImmutableMultiDict, que se comporta como un diccionario
        # para los fines de repoblar el formulario. Todos sus valores son strings.
        datos_formulario = request.form.to_dict() # Convertir a dict regular para más flexibilidad si es necesario

        arti = datos_formulario.get('ARTI')
        cantidad_str = datos_formulario.get('CANTIDAD')
        cod_cte = datos_formulario.get('COD_CTE')
        cod_dir = datos_formulario.get('COD_DIR')
        fecha_exp_str = datos_formulario.get('FECHA_EXP') # Ya es una cadena
        pedido_cte = datos_formulario.get('PEDIDO_CTE')
        usuario = session['user_id']

        # --- VALIDACIÓN DEL ARTÍCULO ---
        if not arti: # Validación básica de campo requerido
            flash("El campo Artículo es obligatorio.", "danger")
            return render_template('pedido_form.html', pedido=datos_formulario)

        conn_val_arti = get_db_connection()
        if not conn_val_arti:
            # El flash ya se habrá mostrado por get_db_connection
            return render_template('pedido_form.html', pedido=datos_formulario)
        
        cursor_val_arti = conn_val_arti.cursor(dictionary=True)
        articulo_valido = False
        try:
            cursor_val_arti.execute("SELECT COD_ART FROM ITMMASTER WHERE COD_ART = %s", (arti,))
            if cursor_val_arti.fetchone():
                articulo_valido = True
            else:
                flash(f"El artículo '{arti}' no existe en el maestro de artículos.", "danger")
        except mysql.connector.Error as err_val_arti:
            flash(f"Error al validar artículo: {err_val_arti}", "danger")
            print(f"ERROR DB Validating ARTI: {err_val_arti}")
        finally:
            if conn_val_arti.is_connected():
                cursor_val_arti.close()
                conn_val_arti.close()
        
        if not articulo_valido:
            return render_template('pedido_form.html', pedido=datos_formulario)
        # --- FIN VALIDACIÓN ARTÍCULO ---

        # --- VALIDACIÓN DEL CÓDIGO DE CLIENTE ---
        cliente_valido = True 
        # Asumimos que COD_CTE puede ser opcional. Si es obligatorio, la lógica cambia.
        if cod_cte and cod_cte.strip(): # Solo validar si se proporciona y no está vacío
            conn_val_cte = get_db_connection()
            if not conn_val_cte:
                return render_template('pedido_form.html', pedido=datos_formulario)
            
            cursor_val_cte = conn_val_cte.cursor(dictionary=True)
            try:
                cursor_val_cte.execute("SELECT COD_CTE FROM CLIENTES_WEB WHERE COD_CTE = %s", (cod_cte,))
                if cursor_val_cte.fetchone():
                    pass # Cliente válido
                else:
                    flash(f"El código de cliente '{cod_cte}' no existe.", "danger")
                    cliente_valido = False
            except mysql.connector.Error as err_val_cte:
                flash(f"Error al validar código de cliente: {err_val_cte}", "danger")
                print(f"ERROR DB Validating COD_CTE: {err_val_cte}")
                cliente_valido = False
            finally:
                if conn_val_cte.is_connected():
                    cursor_val_cte.close()
                    conn_val_cte.close()
        # else: # Si COD_CTE fuera obligatorio y está vacío:
            # flash("El Código de Cliente es obligatorio.", "danger")
            # cliente_valido = False
        
        if not cliente_valido:
            return render_template('pedido_form.html', pedido=datos_formulario)
        # --- FIN VALIDACIÓN CÓDIGO DE CLIENTE ---

        # Validación de cantidad
        if not cantidad_str: # Validación básica de campo requerido
            flash("El campo Cantidad es obligatorio.", "danger")
            return render_template('pedido_form.html', pedido=datos_formulario)
        try:
            cantidad = int(cantidad_str)
            if cantidad <= 0:
                raise ValueError("La cantidad debe ser un número positivo.")
        except ValueError as e:
            flash(f"Cantidad inválida: {e}", "danger")
            return render_template('pedido_form.html', pedido=datos_formulario)

        # Conversión y validación de Fecha de Expiración
        fecha_exp_obj = None # Usaremos este objeto para la BD
        if fecha_exp_str and fecha_exp_str.strip(): # Si se proporciona una fecha
            try:
                fecha_exp_obj = datetime.datetime.strptime(fecha_exp_str, '%Y-%m-%d').date()
            except ValueError:
                flash("Formato de Fecha de Expiración inválido. Usar YYYY-MM-DD.", "danger")
                # datos_formulario['FECHA_EXP'] ya es la cadena incorrecta que el usuario ingresó
                return render_template('pedido_form.html', pedido=datos_formulario)
        # Si fecha_exp_str está vacía, fecha_exp_obj permanecerá como None (asumiendo que es opcional)

        # Si todas las validaciones pasan, procedemos a insertar
        conn_insert = get_db_connection()
        if not conn_insert:
            # Flash ya mostrado
            return render_template('pedido_form.html', pedido=datos_formulario)
        
        cursor_insert = conn_insert.cursor()
        sql = """
            INSERT INTO PEDIDOS_WEB (ARTI, CANTIDAD, COD_CTE, COD_DIR, FECHA_EXP, PEDIDO_CTE, USUARIO)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """
        # Asegurar que se inserta None si los campos opcionales están vacíos
        cod_cte_insert = cod_cte if cod_cte and cod_cte.strip() else None
        cod_dir_insert = cod_dir if cod_dir and cod_dir.strip() else None
        pedido_cte_insert = pedido_cte if pedido_cte and pedido_cte.strip() else None
        
        val = (arti, cantidad, cod_cte_insert, cod_dir_insert, fecha_exp_obj, pedido_cte_insert, usuario)
        
        try:
            cursor_insert.execute(sql, val)
            conn_insert.commit()
            flash("Pedido agregado exitosamente.", "success")
            return redirect(url_for('index'))
        except mysql.connector.Error as err:
            conn_insert.rollback()
            flash(f"Error al agregar pedido: {err}", "danger")
            print(f"ERROR DB Inserting pedido: {err}")
        finally:
            if conn_insert.is_connected():
                cursor_insert.close()
                conn_insert.close()
        
        # Si la inserción falla por alguna razón después de las validaciones (raro, pero posible)
        # volvemos al formulario con los datos.
        return render_template('pedido_form.html', pedido=datos_formulario)

    # Para el método GET (cargar el formulario vacío por primera vez)
    # Pasamos un diccionario vacío o None para que 'pedido.CAMPO' no dé error en la plantilla
    return render_template('pedido_form.html', pedido={}) # o pedido=None


@app.route('/pedidos/editar/<int:id>', methods=['GET', 'POST'])
@login_required
def editar_pedido(id):
    if request.method == 'POST':
        # Recoger datos del formulario como cadenas
        datos_formulario = request.form.to_dict()
        datos_formulario['ID'] = id # Añadir ID para repoblar si hay error

        arti = datos_formulario.get('ARTI')
        cantidad_str = datos_formulario.get('CANTIDAD')
        cod_cte = datos_formulario.get('COD_CTE')
        cod_dir = datos_formulario.get('COD_DIR')
        fecha_exp_str = datos_formulario.get('FECHA_EXP') # Ya es una cadena
        pedido_cte = datos_formulario.get('PEDIDO_CTE')
        usuario = session['user_id'] # El usuario no debería cambiar

        # --- VALIDACIÓN DEL ARTÍCULO (si es editable) ---
        if not arti:
            flash("El campo Artículo es obligatorio.", "danger")
            return render_template('pedido_form.html', pedido=datos_formulario)
        
        conn_val_arti = get_db_connection()
        if not conn_val_arti:
            return render_template('pedido_form.html', pedido=datos_formulario)
        cursor_val_arti = conn_val_arti.cursor(dictionary=True)
        articulo_valido = False
        try:
            cursor_val_arti.execute("SELECT COD_ART FROM ITMMASTER WHERE COD_ART = %s", (arti,))
            if cursor_val_arti.fetchone():
                articulo_valido = True
            else:
                flash(f"El artículo '{arti}' no existe en el maestro de artículos.", "danger")
        except mysql.connector.Error as err_val_arti:
            flash(f"Error al validar artículo: {err_val_arti}", "danger")
            print(f"ERROR DB Validating ARTI on edit: {err_val_arti}")
        finally:
            if conn_val_arti.is_connected():
                cursor_val_arti.close()
                conn_val_arti.close()
        
        if not articulo_valido:
            return render_template('pedido_form.html', pedido=datos_formulario)
        # --- FIN VALIDACIÓN ARTÍCULO ---

        # --- VALIDACIÓN DEL CÓDIGO DE CLIENTE (si es editable) ---
        cliente_valido = True
        if cod_cte and cod_cte.strip():
            conn_val_cte = get_db_connection()
            if not conn_val_cte:
                return render_template('pedido_form.html', pedido=datos_formulario)
            cursor_val_cte = conn_val_cte.cursor(dictionary=True)
            try:
                cursor_val_cte.execute("SELECT COD_CTE FROM CLIENTES_WEB WHERE COD_CTE = %s", (cod_cte,))
                if cursor_val_cte.fetchone():
                    pass 
                else:
                    flash(f"El código de cliente '{cod_cte}' no existe.", "danger")
                    cliente_valido = False
            except mysql.connector.Error as err_val_cte:
                flash(f"Error al validar código de cliente: {err_val_cte}", "danger")
                print(f"ERROR DB Validating COD_CTE on edit: {err_val_cte}")
                cliente_valido = False
            finally:
                if conn_val_cte.is_connected():
                    cursor_val_cte.close()
                    conn_val_cte.close()
        # else: # Si COD_CTE fuera obligatorio y está vacío:
            # flash("El Código de Cliente es obligatorio.", "danger")
            # cliente_valido = False
            
        if not cliente_valido:
            return render_template('pedido_form.html', pedido=datos_formulario)
        # --- FIN VALIDACIÓN CÓDIGO DE CLIENTE ---

        # Validación de cantidad
        if not cantidad_str:
            flash("El campo Cantidad es obligatorio.", "danger")
            return render_template('pedido_form.html', pedido=datos_formulario)
        try:
            cantidad = int(cantidad_str)
            if cantidad <= 0:
                raise ValueError("La cantidad debe ser un número positivo.")
        except ValueError as e:
            flash(f"Cantidad inválida: {e}", "danger")
            return render_template('pedido_form.html', pedido=datos_formulario)

        # Conversión y validación de Fecha de Expiración
        fecha_exp_obj = None
        if fecha_exp_str and fecha_exp_str.strip():
            try:
                fecha_exp_obj = datetime.datetime.strptime(fecha_exp_str, '%Y-%m-%d').date()
            except ValueError:
                flash("Formato de Fecha de Expiración inválido. Usar YYYY-MM-DD.", "danger")
                return render_template('pedido_form.html', pedido=datos_formulario)
        
        # Si todas las validaciones pasan, procedemos a actualizar
        conn_update = get_db_connection()
        if not conn_update:
            return render_template('pedido_form.html', pedido=datos_formulario)
        
        cursor_update = conn_update.cursor()
        sql_update = """
            UPDATE PEDIDOS_WEB
            SET ARTI = %s, CANTIDAD = %s, COD_CTE = %s, COD_DIR = %s,
                FECHA_EXP = %s, PEDIDO_CTE = %s
            WHERE ID = %s AND USUARIO = %s 
        """ # Importante: AND USUARIO = %s para seguridad
        
        cod_cte_update = cod_cte if cod_cte and cod_cte.strip() else None
        cod_dir_update = cod_dir if cod_dir and cod_dir.strip() else None
        pedido_cte_update = pedido_cte if pedido_cte and pedido_cte.strip() else None

        val_update = (
            arti, cantidad, cod_cte_update, cod_dir_update, 
            fecha_exp_obj, pedido_cte_update, 
            id, session['user_id'] # id del pedido y usuario de la sesión
        )

        try:
            cursor_update.execute(sql_update, val_update)
            # Verificar si alguna fila fue afectada para confirmar la actualización y pertenencia
            if cursor_update.rowcount == 0:
                flash("No se pudo actualizar el pedido. Puede que no exista o no tengas permiso.", "warning")
            else:
                conn_update.commit()
                flash("Pedido actualizado exitosamente.", "success")
            return redirect(url_for('index'))
        except mysql.connector.Error as err:
            conn_update.rollback()
            flash(f"Error al actualizar pedido: {err}", "danger")
            print(f"ERROR DB Updating pedido: {err}")
        finally:
            if conn_update.is_connected():
                cursor_update.close()
                conn_update.close()
        
        # Si la actualización falla
        return render_template('pedido_form.html', pedido=datos_formulario)

    # --- MÉTODO GET: Cargar datos para el formulario de edición ---
    else: # request.method == 'GET'
        conn_get = get_db_connection()
        if not conn_get:
            # Flash ya se mostró, redirigir a index
            return redirect(url_for('index'))

        cursor_get = conn_get.cursor(dictionary=True)
        pedido_db = None
        try:
            # Seleccionar el pedido específico del usuario logueado
            cursor_get.execute("SELECT * FROM PEDIDOS_WEB WHERE ID = %s AND USUARIO = %s", (id, session['user_id']))
            pedido_db = cursor_get.fetchone()
        except mysql.connector.Error as err:
            flash(f"Error al cargar el pedido para editar: {err}", "danger")
            print(f"ERROR DB fetching pedido for edit (GET): {err}")
            if conn_get.is_connected():
                cursor_get.close()
                conn_get.close()
            return redirect(url_for('index'))

        if not pedido_db:
            flash("Pedido no encontrado o no tienes permiso para editarlo.", "warning")
            if conn_get.is_connected():
                cursor_get.close() # Cerrar si aún está abierto
                conn_get.close()
            return redirect(url_for('index'))

        # Preparamos el diccionario para la plantilla, convirtiendo FECHA_EXP a string si existe
        pedido_para_plantilla = pedido_db.copy() # Trabajar con una copia

        if pedido_para_plantilla.get('FECHA_EXP') and isinstance(pedido_para_plantilla['FECHA_EXP'], (datetime.date, datetime.datetime)):
            # Convertir objeto date/datetime a cadena 'YYYY-MM-DD'
            pedido_para_plantilla['FECHA_EXP'] = pedido_para_plantilla['FECHA_EXP'].strftime('%Y-%m-%d')
        elif not pedido_para_plantilla.get('FECHA_EXP'):
            # Si FECHA_EXP es None en la BD, asegurar que sea una cadena vacía para el input
            pedido_para_plantilla['FECHA_EXP'] = ''
        # Si ya es una cadena (poco probable desde BD directamente, pero por si acaso), se queda como está.
        
        # Asegurar que otros campos que podrían ser None sean cadenas vacías si es necesario para el value
        for key in ['ARTI', 'CANTIDAD', 'COD_CTE', 'COD_DIR', 'PEDIDO_CTE']:
            if pedido_para_plantilla.get(key) is None:
                pedido_para_plantilla[key] = ''
            # Convertir cantidad a string para el input si no lo es (aunque usualmente el conector ya lo hace)
            if key == 'CANTIDAD' and not isinstance(pedido_para_plantilla[key], str):
                 pedido_para_plantilla[key] = str(pedido_para_plantilla[key])


        if conn_get.is_connected():
            cursor_get.close()
            conn_get.close()
        
        return render_template('pedido_form.html', pedido=pedido_para_plantilla)


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

@app.route('/sugerencias_articulo')
@login_required
def sugerencias_articulo():
    query_param = request.args.get('q', '')
    sugerencias_list = []

    if len(query_param) < 2: # No buscar si la consulta es muy corta
        return jsonify(sugerencias=[])

    conn = get_db_connection()
    if not conn:
        return jsonify(error="Error de conexión a la base de datos"), 500

    cursor = conn.cursor(dictionary=True)
    try:
        # Buscamos en COD_ART y DESCRIPCION
        # El '%' al final busca que comiencen con query_param
        sql_query = """
            SELECT COD_ART, DESCRIPCION 
            FROM ITMMASTER 
            WHERE COD_ART LIKE %s OR DESCRIPCION LIKE %s 
            LIMIT 10
        """
        search_term = query_param + '%'
        cursor.execute(sql_query, (search_term, search_term)) # Pasamos dos veces el search_term
        
        resultados = cursor.fetchall()
        for row in resultados:
            sugerencias_list.append({'cod_art': row['COD_ART'], 'descripcion': row['DESCRIPCION']})

    except mysql.connector.Error as err:
        print(f"Error en la base de datos al obtener sugerencias: {err}")
        return jsonify(error=f"Error de base de datos: {err}"), 500 # Es mejor no exponer el error SQL al cliente
    finally:
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

if __name__ == '__main__':
    app.run(debug=True) # debug=True solo para desarrollo
