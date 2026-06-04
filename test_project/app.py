#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Aplicación Web para visualización de resumen de pedidos
"""

import os
from functools import wraps
from flask import Flask, render_template, jsonify, request, session, redirect, url_for
from dotenv import load_dotenv
from database import db

# Cargar variables de entorno
load_dotenv()

# Crear aplicación Flask
app = Flask(__name__,
            template_folder='templates',
            static_folder='static')

app.secret_key = os.getenv('SECRET_KEY', 'clave_secreta_por_defecto')

# Credenciales de autenticación
USUARIO_AUTENTICACION = os.getenv('APP_USER', 'famesa')
PASSWORD_AUTENTICACION = os.getenv('APP_PASSWORD', 'Famesa123')

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
    """Página principal con la tabla de resumen de pedidos"""
    return render_template('index.html', username=session.get('username', ''))

@app.route('/pedidos/<usuario>')
@login_required
def pedidos_list(usuario):
    """Página de lista de pedidos por usuario"""
    return render_template('pedidos_list.html', usuario=usuario)

@app.route('/pedido/<usuario>/<numped>')
@login_required
def pedido_detalle(usuario, numped):
    """Página de detalle de un pedido específico"""
    return render_template('pedido_detalle.html', usuario=usuario, numped=numped)

@app.route('/api/health')
def health_check():
    """Endpoint para verificar el estado de la aplicación (no requiere auth)"""
    try:
        if db.connection and db.connection.is_connected():
            db_status = "Conectado"
        else:
            db_status = "Desconectado"
        
        return jsonify({
            'status': 'OK',
            'database': db_status,
            'version': '1.0.0'
        })
    except Exception as e:
        return jsonify({
            'status': 'ERROR',
            'message': str(e)
        }), 500

@app.route('/api/pedidos')
@login_required
def get_pedidos_api():
    """API para obtener datos de pedidos en formato JSON"""
    try:
        # Intentar conectar si no está conectado
        if not db.connection or not db.connection.is_connected():
            if not db.create_connection():
                return jsonify({
                    'success': False,
                    'message': 'No se pudo conectar a la base de datos'
                }), 503
        
        # Obtener datos
        datos = db.get_pedidos_summary()
        
        if datos is None:
            return jsonify({
                'success': False,
                'message': 'Error al obtener datos de la base de datos'
            }), 500
        
        # Formatear datos para la respuesta
        resultado = []
        for fila in datos:
            resultado.append({
                'usuario': fila['USUARIO'],
                'nombre': fila['NOMBRE'] or '',
                'num_pedidos': fila['NUM_PEDIDOS'],
                'importe_total': float(fila['IMPORTE_TOTAL']) if fila['IMPORTE_TOTAL'] else 0.0,
                'importe_medio': float(fila['IMPORTE_MEDIO_POR_PEDIDO']) if fila['IMPORTE_MEDIO_POR_PEDIDO'] else 0.0
            })
        
        return jsonify({
            'success': True,
            'data': resultado,
            'total_registros': len(resultado)
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'Error en el servidor: {str(e)}'
        }), 500

@app.route('/api/pedidos/<usuario>')
@login_required
def get_pedidos_by_usuario(usuario):
    """API para obtener pedidos de un usuario específico"""
    try:
        if not db.connection or not db.connection.is_connected():
            if not db.create_connection():
                return jsonify({
                    'success': False,
                    'message': 'No se pudo conectar a la base de datos'
                }), 503
        
        datos = db.get_pedidos_by_usuario(usuario)
        
        if datos is None:
            return jsonify({
                'success': False,
                'message': 'Error al obtener datos de la base de datos'
            }), 500
        
        resultado = []
        for fila in datos:
            resultado.append({
                'usuario': fila['USUARIO'],
                'nombre': fila['NOMBRE'] or '',
                'numped': fila['NUMPED'],
                'estado': fila['ESTADO'] or '',
                'lineas': fila['LINEAS'],
                'importe': float(fila['IMPORTE']) if fila['IMPORTE'] else 0.0
            })
        
        return jsonify({
            'success': True,
            'data': resultado,
            'total_registros': len(resultado)
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'Error en el servidor: {str(e)}'
        }), 500

@app.route('/api/pedido/<usuario>/<numped>')
@login_required
def get_pedido_detalle(usuario, numped):
    """API para obtener detalle completo de un pedido"""
    try:
        if not db.connection or not db.connection.is_connected():
            if not db.create_connection():
                return jsonify({
                    'success': False,
                    'message': 'No se pudo conectar a la base de datos'
                }), 503
        
        # Obtener resumen del pedido
        resumen = db.get_pedido_resumen(usuario, numped)
        if not resumen or len(resumen) == 0:
            return jsonify({
                'success': False,
                'message': 'Pedido no encontrado'
            }), 404
        
        # Obtener líneas del pedido
        lineas = db.get_pedido_detalle(usuario, numped)
        if lineas is None:
            return jsonify({
                'success': False,
                'message': 'Error al obtener detalle del pedido'
            }), 500
        
        # Formatear resumen
        resumen_data = {
            'numped': resumen[0]['NUMPED'],
            'usuario': resumen[0]['USUARIO'],
            'nombre': resumen[0]['NOMBRE'] or '',
            'estado': resumen[0]['ESTADO'],
            'total_lineas': resumen[0]['TOTAL_LINEAS'],
            'importe_total': float(resumen[0]['IMPORTE_TOTAL']) if resumen[0]['IMPORTE_TOTAL'] else 0.0,
            'fecha_pedido': resumen[0]['FECHA_PEDIDO'].strftime('%d/%m/%Y %H:%M') if resumen[0]['FECHA_PEDIDO'] else ''
        }
        
        # Formatear líneas
        lineas_data = []
        for linea in lineas:
            lineas_data.append({
                'codart': linea['CODART'] or '',
                'desart': linea['DESART'] or '',
                'cantidad': float(linea['CANTIDAD']) if linea['CANTIDAD'] else 0.0,
                'precio': float(linea['PRECIO']) if linea['PRECIO'] else 0.0,
                'descuento1': float(linea['DESCUENTO1']) if linea['DESCUENTO1'] else 0.0,
                'descuento2': float(linea['DESCUENTO2']) if linea['DESCUENTO2'] else 0.0,
                'importe_linea': float(linea['IMPORTE_LINEA']) if linea['IMPORTE_LINEA'] else 0.0
            })
        
        return jsonify({
            'success': True,
            'resumen': resumen_data,
            'lineas': lineas_data,
            'total_lineas': len(lineas_data)
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'Error en el servidor: {str(e)}'
        }), 500

if __name__ == '__main__':
    # Configuración para desarrollo
    host = os.getenv('HOST', '0.0.0.0')
    port = int(os.getenv('PORT', 5000))
    debug = os.getenv('FLASK_ENV') != 'production'
    
    print(f"Iniciando aplicación en {host}:{port}")
    print(f"Modo: {'Desarrollo' if debug else 'Producción'}")
    
    # Iniciar aplicación
    if debug:
        app.run(host=host, port=port, debug=debug)
    else:
        # Para producción usar waitress (mejor en Windows) o gunicorn
        try:
            from waitress import serve
            serve(app, host=host, port=port)
        except ImportError:
            app.run(host=host, port=port, debug=False)