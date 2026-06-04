#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Aplicación Web para Gestión de Fichas de Paletización
"""

import os
from functools import wraps
from flask import Flask, render_template, request, redirect, url_for, flash, jsonify, session
from database import db

# Crear aplicación Flask
app = Flask(__name__,
            template_folder='templates',
            static_folder='static')

app.secret_key = os.getenv('SECRET_KEY', 'clave_secreta_dal_ia_2024')

# Credenciales de autenticación
USUARIO_AUTENTICACION = os.getenv('APP_USER', 'dilies')
PASSWORD_AUTENTICACION = os.getenv('APP_PASSWORD', 'dalia123')

def login_required(f):
    """Decorador para requerir autenticación"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not session.get('logged_in'):
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

@app.route('/login', methods=['GET', 'POST'])
def login():
    """Página de inicio de sesión"""
    error = None
    if request.method == 'POST':
        username = request.form.get('username', '')
        password = request.form.get('password', '')
        
        if username == USUARIO_AUTENTICACION and password == PASSWORD_AUTENTICACION:
            session['logged_in'] = True
            session['username'] = username
            return redirect(url_for('index'))
        else:
            error = 'Usuario o contraseña incorrectos'
    
    return render_template('login.html', error=error)

@app.route('/logout')
def logout():
    """Cerrar sesión"""
    session.clear()
    return redirect(url_for('login'))

@app.route('/')
@login_required
def index():
    """Página principal con listado de fichas"""
    fichas = obtener_fichas()
    return render_template('index.html', fichas=fichas)

def obtener_fichas():
    """Obtener todas las fichas de paletización"""
    try:
        query = 'SELECT * FROM fichas_paletizacion ORDER BY fecha_creacion DESC'
        resultados = db.execute_query(query)
        return resultados if resultados else []
    except Exception as e:
        print(f"Error al obtener fichas: {e}")
        return []

def obtener_ficha_por_id(ficha_id):
    """Obtener una ficha específica por su ID"""
    try:
        query = 'SELECT * FROM fichas_paletizacion WHERE id = %s'
        resultados = db.execute_query(query, (ficha_id,))
        return resultados[0] if resultados and len(resultados) > 0 else None
    except Exception as e:
        print(f"Error al obtener ficha: {e}")
        return None

def crear_ficha(datos):
    """Crear una nueva ficha de paletización"""
    try:
        query = '''
            INSERT INTO fichas_paletizacion (
                cliente, numero_cliente, destino,
                empaquetado_individual,
                etiquetado_especial, etiquetado_especial_detalle,
                separacion_referencias, separacion_referencias_detalle,
                documentacion_adjunta, documentacion_adjunta_detalle,
                altura_maxima, peso_maximo, tipo_palet, palet_apilable, distribucion_mercancia,
                lleva_carteles, lleva_carteles_detalle,
                etiquetas, etiquetas_detalle1, etiquetas_detalle2, etiquetas_detalle3,
                codigo_sscc, codigo_sscc_detalle,
                packing_list, packing_list_detalle,
                etiqueta_transporte, etiqueta_transporte_detalle,
                observaciones, logo_data
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        '''
        
        params = (
            datos.get('cliente', ''),
            datos.get('numero_cliente', ''),
            datos.get('destino', ''),
            int(datos.get('empaquetado_individual', 0)),
            int(datos.get('etiquetado_especial', 0)),
            datos.get('etiquetado_especial_detalle', ''),
            int(datos.get('separacion_referencias', 0)),
            datos.get('separacion_referencias_detalle', ''),
            int(datos.get('documentacion_adjunta', 0)),
            datos.get('documentacion_adjunta_detalle', ''),
            datos.get('altura_maxima', ''),
            datos.get('peso_maximo', ''),
            datos.get('tipo_palet', ''),
            int(datos.get('palet_apilable', 0)),
            datos.get('distribucion_mercancia', ''),
            int(datos.get('lleva_carteles', 0)),
            datos.get('lleva_carteles_detalle', ''),
            int(datos.get('etiquetas', 0)),
            datos.get('etiquetas_detalle1', ''),
            datos.get('etiquetas_detalle2', ''),
            datos.get('etiquetas_detalle3', ''),
            int(datos.get('codigo_sscc', 0)),
            datos.get('codigo_sscc_detalle', ''),
            int(datos.get('packing_list', 0)),
            datos.get('packing_list_detalle', ''),
            int(datos.get('etiqueta_transporte', 0)),
            datos.get('etiqueta_transporte_detalle', ''),
            datos.get('observaciones', ''),
            datos.get('logo_data', '')
        )
        
        ficha_id = db.execute_update(query, params)
        return ficha_id
    except Exception as e:
        print(f"Error al crear ficha: {e}")
        return None

def actualizar_ficha(ficha_id, datos):
    """Actualizar una ficha existente"""
    try:
        query = '''
            UPDATE fichas_paletizacion SET
                cliente = %s, numero_cliente = %s, destino = %s,
                empaquetado_individual = %s,
                etiquetado_especial = %s, etiquetado_especial_detalle = %s,
                separacion_referencias = %s, separacion_referencias_detalle = %s,
                documentacion_adjunta = %s, documentacion_adjunta_detalle = %s,
                altura_maxima = %s, peso_maximo = %s, tipo_palet = %s, palet_apilable = %s, distribucion_mercancia = %s,
                lleva_carteles = %s, lleva_carteles_detalle = %s,
                etiquetas = %s, etiquetas_detalle1 = %s, etiquetas_detalle2 = %s, etiquetas_detalle3 = %s,
                codigo_sscc = %s, codigo_sscc_detalle = %s,
                packing_list = %s, packing_list_detalle = %s,
                etiqueta_transporte = %s, etiqueta_transporte_detalle = %s,
                observaciones = %s, logo_data = %s
            WHERE id = %s
        '''
        
        params = (
            datos.get('cliente', ''),
            datos.get('numero_cliente', ''),
            datos.get('destino', ''),
            int(datos.get('empaquetado_individual', 0)),
            int(datos.get('etiquetado_especial', 0)),
            datos.get('etiquetado_especial_detalle', ''),
            int(datos.get('separacion_referencias', 0)),
            datos.get('separacion_referencias_detalle', ''),
            int(datos.get('documentacion_adjunta', 0)),
            datos.get('documentacion_adjunta_detalle', ''),
            datos.get('altura_maxima', ''),
            datos.get('peso_maximo', ''),
            datos.get('tipo_palet', ''),
            int(datos.get('palet_apilable', 0)),
            datos.get('distribucion_mercancia', ''),
            int(datos.get('lleva_carteles', 0)),
            datos.get('lleva_carteles_detalle', ''),
            int(datos.get('etiquetas', 0)),
            datos.get('etiquetas_detalle1', ''),
            datos.get('etiquetas_detalle2', ''),
            datos.get('etiquetas_detalle3', ''),
            int(datos.get('codigo_sscc', 0)),
            datos.get('codigo_sscc_detalle', ''),
            int(datos.get('packing_list', 0)),
            datos.get('packing_list_detalle', ''),
            int(datos.get('etiqueta_transporte', 0)),
            datos.get('etiqueta_transporte_detalle', ''),
            datos.get('observaciones', ''),
            datos.get('logo_data', ''),
            ficha_id
        )
        
        db.execute_update(query, params)
        return True
    except Exception as e:
        print(f"Error al actualizar ficha: {e}")
        return False

def eliminar_ficha(ficha_id):
    """Eliminar una ficha de paletización"""
    try:
        query = 'DELETE FROM fichas_paletizacion WHERE id = %s'
        db.execute_update(query, (ficha_id,))
        return True
    except Exception as e:
        print(f"Error al eliminar ficha: {e}")
        return False

@app.route('/ficha/nueva', methods=['GET', 'POST'])
@login_required
def ficha_nueva():
    """Crear nueva ficha de paletización"""
    if request.method == 'POST':
        datos = {
            'cliente': request.form.get('cliente', ''),
            'numero_cliente': request.form.get('numero_cliente', ''),
            'destino': request.form.get('destino', ''),
            'empaquetado_individual': request.form.get('empaquetado_individual', '0'),
            'etiquetado_especial': request.form.get('etiquetado_especial', '0'),
            'etiquetado_especial_detalle': request.form.get('etiquetado_especial_detalle', ''),
            'separacion_referencias': request.form.get('separacion_referencias', '0'),
            'separacion_referencias_detalle': request.form.get('separacion_referencias_detalle', ''),
            'documentacion_adjunta': request.form.get('documentacion_adjunta', '0'),
            'documentacion_adjunta_detalle': request.form.get('documentacion_adjunta_detalle', ''),
            'altura_maxima': request.form.get('altura_maxima', ''),
            'peso_maximo': request.form.get('peso_maximo', ''),
            'tipo_palet': request.form.get('tipo_palet', ''),
            'palet_apilable': request.form.get('palet_apilable', '0'),
            'distribucion_mercancia': request.form.get('distribucion_mercancia', ''),
            'lleva_carteles': request.form.get('lleva_carteles', '0'),
            'lleva_carteles_detalle': request.form.get('lleva_carteles_detalle', ''),
            'etiquetas': request.form.get('etiquetas', '0'),
            'etiquetas_detalle1': request.form.get('etiquetas_detalle1', ''),
            'etiquetas_detalle2': request.form.get('etiquetas_detalle2', ''),
            'etiquetas_detalle3': request.form.get('etiquetas_detalle3', ''),
            'codigo_sscc': request.form.get('codigo_sscc', '0'),
            'codigo_sscc_detalle': request.form.get('codigo_sscc_detalle', ''),
            'packing_list': request.form.get('packing_list', '0'),
            'packing_list_detalle': request.form.get('packing_list_detalle', ''),
            'etiqueta_transporte': request.form.get('etiqueta_transporte', '0'),
            'etiqueta_transporte_detalle': request.form.get('etiqueta_transporte_detalle', ''),
            'observaciones': request.form.get('observaciones', ''),
            'logo_data': request.form.get('logo_data', '')
        }
        
        ficha_id = crear_ficha(datos)
        if ficha_id:
            flash('Ficha de paletización creada exitosamente', 'success')
            return redirect(url_for('ver_ficha', ficha_id=ficha_id))
        else:
            flash('Error al crear la ficha', 'error')
    
    return render_template('ficha_form.html', ficha=None)

@app.route('/ficha/<int:ficha_id>')
@login_required
def ver_ficha(ficha_id):
    """Ver detalle de una ficha"""
    ficha = obtener_ficha_por_id(ficha_id)
    if not ficha:
        flash('Ficha no encontrada', 'error')
        return redirect(url_for('index'))
    return render_template('ficha_ver.html', ficha=ficha)

@app.route('/ficha/<int:ficha_id>/editar', methods=['GET', 'POST'])
@login_required
def editar_ficha(ficha_id):
    """Editar una ficha existente"""
    ficha = obtener_ficha_por_id(ficha_id)
    if not ficha:
        flash('Ficha no encontrada', 'error')
        return redirect(url_for('index'))
    
    if request.method == 'POST':
        datos = {
            'cliente': request.form.get('cliente', ''),
            'numero_cliente': request.form.get('numero_cliente', ''),
            'destino': request.form.get('destino', ''),
            'empaquetado_individual': request.form.get('empaquetado_individual', '0'),
            'etiquetado_especial': request.form.get('etiquetado_especial', '0'),
            'etiquetado_especial_detalle': request.form.get('etiquetado_especial_detalle', ''),
            'separacion_referencias': request.form.get('separacion_referencias', '0'),
            'separacion_referencias_detalle': request.form.get('separacion_referencias_detalle', ''),
            'documentacion_adjunta': request.form.get('documentacion_adjunta', '0'),
            'documentacion_adjunta_detalle': request.form.get('documentacion_adjunta_detalle', ''),
            'altura_maxima': request.form.get('altura_maxima', ''),
            'peso_maximo': request.form.get('peso_maximo', ''),
            'tipo_palet': request.form.get('tipo_palet', ''),
            'palet_apilable': request.form.get('palet_apilable', '0'),
            'distribucion_mercancia': request.form.get('distribucion_mercancia', ''),
            'lleva_carteles': request.form.get('lleva_carteles', '0'),
            'lleva_carteles_detalle': request.form.get('lleva_carteles_detalle', ''),
            'etiquetas': request.form.get('etiquetas', '0'),
            'etiquetas_detalle1': request.form.get('etiquetas_detalle1', ''),
            'etiquetas_detalle2': request.form.get('etiquetas_detalle2', ''),
            'etiquetas_detalle3': request.form.get('etiquetas_detalle3', ''),
            'codigo_sscc': request.form.get('codigo_sscc', '0'),
            'codigo_sscc_detalle': request.form.get('codigo_sscc_detalle', ''),
            'packing_list': request.form.get('packing_list', '0'),
            'packing_list_detalle': request.form.get('packing_list_detalle', ''),
            'etiqueta_transporte': request.form.get('etiqueta_transporte', '0'),
            'etiqueta_transporte_detalle': request.form.get('etiqueta_transporte_detalle', ''),
            'observaciones': request.form.get('observaciones', ''),
            'logo_data': request.form.get('logo_data', '')
        }
        
        if actualizar_ficha(ficha_id, datos):
            flash('Ficha de paletización actualizada exitosamente', 'success')
            return redirect(url_for('ver_ficha', ficha_id=ficha_id))
        else:
            flash('Error al actualizar la ficha', 'error')
    
    return render_template('ficha_form.html', ficha=ficha)

@app.route('/ficha/<int:ficha_id>/eliminar', methods=['POST'])
@login_required
def eliminar_ficha_route(ficha_id):
    """Eliminar una ficha"""
    eliminar_ficha(ficha_id)
    flash('Ficha de paletización eliminada exitosamente', 'success')
    return redirect(url_for('index'))

# API endpoints
@app.route('/api/fichas')
@login_required
def api_get_fichas():
    """API para obtener todas las fichas"""
    try:
        fichas = obtener_fichas()
        return jsonify({
            'success': True,
            'data': fichas,
            'total': len(fichas)
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@app.route('/api/ficha/<int:ficha_id>')
@login_required
def api_get_ficha(ficha_id):
    """API para obtener una ficha específica"""
    try:
        ficha = obtener_ficha_por_id(ficha_id)
        if not ficha:
            return jsonify({
                'success': False,
                'message': 'Ficha no encontrada'
            }), 404
        
        return jsonify({
            'success': True,
            'data': ficha
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@app.route('/api/ficha/crear', methods=['POST'])
@login_required
def api_crear_ficha():
    """API para crear una ficha"""
    try:
        datos = request.get_json()
        if not datos:
            return jsonify({
                'success': False,
                'message': 'No se recibieron datos'
            }), 400
        
        ficha_id = crear_ficha(datos)
        if ficha_id:
            return jsonify({
                'success': True,
                'id': ficha_id,
                'message': 'Ficha creada exitosamente'
            })
        else:
            return jsonify({
                'success': False,
                'message': 'Error al crear la ficha'
            }), 500
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@app.route('/api/clientes')
@login_required
def api_get_clientes():
    """API para obtener lista de clientes desde dwdb.DIM_CTE"""
    try:
        clientes = db.get_clientes()
        return jsonify({
            'success': True,
            'data': clientes,
            'total': len(clientes) if clientes else 0
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@app.route('/api/cliente/<cte_cod>')
@login_required
def api_get_cliente(cte_cod):
    """API para obtener un cliente específico por su código"""
    try:
        cliente = db.get_cliente_por_codigo(cte_cod)
        if not cliente:
            return jsonify({
                'success': False,
                'message': 'Cliente no encontrado'
            }), 404
        
        return jsonify({
            'success': True,
            'data': {
                'cte_cod': cliente['CTE_COD'],
                'cte_razon_social': cliente['CTE_RAZON_SOCIAL']
            }
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@app.route('/api/health')
def health_check():
    """Endpoint para verificar el estado de la aplicación"""
    try:
        if db.connection and db.connection.is_connected():
            db_status = "Conectado"
        else:
            db_status = "Desconectado"
        
        return jsonify({
            'status': 'OK',
            'database': f"MySQL ({db_status})",
            'version': '1.0.0'
        })
    except Exception as e:
        return jsonify({
            'status': 'ERROR',
            'message': str(e)
        }), 500

if __name__ == '__main__':
    # Configuración para desarrollo
    host = os.getenv('HOST', '0.0.0.0')
    port = int(os.getenv('PORT', 5001))
    debug = os.getenv('FLASK_ENV') != 'production'
    
    print(f"Iniciando aplicación DAL-IA en {host}:{port}")
    print(f"Base de datos: {db.host}/{db.database}")
    print(f"Modo: {'Desarrollo' if debug else 'Producción'}")
    
    # Iniciar aplicación
    if debug:
        app.run(host=host, port=port, debug=debug)
    else:
        try:
            from waitress import serve
            serve(app, host=host, port=port)
        except ImportError:
            app.run(host=host, port=port, debug=False)