import os
import json
from flask import Flask, render_template, jsonify, request, Response
import requests
from dotenv import load_dotenv
from datetime import datetime, date
import mysql.connector
import base64

from database import get_albaranes_from_db

load_dotenv()

app = Flask(__name__)

# --- Configuración de la base de datos MySQL para el log ---
db_config_mysql = {
    'host': '192.168.35.25',
    'user': 'mmarco',
    'password': '@System345',
    'database': 'pract'
}

def get_mysql_connection():
    """Establece conexión con la base de datos MySQL."""
    return mysql.connector.connect(**db_config_mysql)


# --- Funciones de Lógica de Negocio ---

def crear_orden_dachser(albaran_data, force=False):
    """
    Prepara y envía la orden de transporte a la API de Dachser.
    """
    base_url = os.environ.get('DACHSER_API_BASE_URL', 'https://api-gateway.dachser.com')
    api_key = os.environ.get('DACHSER_API_KEY')
    consignor_id = os.environ.get('DACHSER_CONSIGNOR_ID')
    forwarder_id = os.environ.get('DACHSER_FORWARDER_ID')

    if not all([api_key, consignor_id, forwarder_id]):
        return {"error": "Faltan variables de entorno críticas (DACHSER_API_KEY, DACHSER_CONSIGNOR_ID o DACHSER_FORWARDER_ID)"}, 500

    # --- VERIFICACIÓN EN EL LOG DE ENVÍOS ---
    num_albaran_actual = albaran_data.get('NUMALB')
    if not num_albaran_actual:
        return {"error": "El albarán no tiene número (NUMALB)."}, 400

    if not force:
        try:
            conn_mysql = get_mysql_connection()
            cursor = conn_mysql.cursor(dictionary=True)
            cursor.execute("SELECT id_orden_transporte FROM LOG_ENVIOS WHERE num_albaran = %s", (num_albaran_actual,))
            envio_existente = cursor.fetchone()
            cursor.close()
            conn_mysql.close()
            if envio_existente:
                return {"error": f"El albarán {num_albaran_actual} ya fue enviado.", "details": f"ID de orden existente: {envio_existente['id_orden_transporte']}"}, 409 # 409 Conflict
        except mysql.connector.Error as err:
            return {"error": "Error al verificar el log de envíos en la base de datos.", "details": str(err)}, 500

    # Mapeo de datos de nuestro albarán al formato de Dachser
    try:
        # 1. Validar la fecha de transporte
        # CORRECCIÓN: La fecha ya viene en formato 'YYYY-MM-DD' desde la BD.
        transport_date_str = albaran_data['FECHA_ENVIO']
        transport_date_obj = datetime.strptime(transport_date_str, '%Y-%m-%d').date()
        today = date.today()
        three_years_from_now = today.replace(year=today.year + 3)

        if transport_date_obj < today:
            return {"error": "La fecha de transporte no puede ser anterior a la fecha actual.", "details": f"Fecha proporcionada: {transport_date_str}"}, 400
        if transport_date_obj > three_years_from_now:
            return {"error": "La fecha de transporte no puede ser superior a 3 años desde la fecha actual.", "details": f"Fecha proporcionada: {transport_date_str}"}, 400

        # Construcción de la información de dirección
        address_info = {
            "streets": [albaran_data['DIR_CONSIG']],
            "city": albaran_data['CIUDAD'],
            "postalCode": albaran_data['COD_POSTAL'],
            "countryCode": albaran_data['PAIS']
        }
        if albaran_data.get('DIR_CONSIG2'):
            address_info["supplementInformation"] = albaran_data['DIR_CONSIG2']

        payload = {
            "transportDate": transport_date_str,
            "division": "T", # T=Industrial Goods
            "product": "Y",   # targoflex
            "term": "031",    # DAP
            "forwarder": {
                "id": forwarder_id
            },
            "consignor": {
                "id": consignor_id
            },
            "consignee": {
                "names": [albaran_data['NOMBRE_CONSIGNATARIO']],
                "addressInformation": address_info
            },
            "references": [
                {
                    "code": "100", # Customer Order Number (Delivery Note)
                    "value": albaran_data['NUMALB']
                }
            ],
            "transportOrderLines": []
        }

        # Añadir observaciones si están presentes en los datos del albarán
        if albaran_data.get('OBSERVACIONES'):
            # Inicializar 'texts' si no existe
            if 'texts' not in payload:
                payload['texts'] = []
            payload['texts'].append({
                "code": "ZU", # Código estándar para "Zusatzinformation" (información adicional)
                "value": albaran_data['OBSERVACIONES']
            })

        # Añadir opciones de entrega (teléfono) si están presentes
        if albaran_data.get('TFNO') and len(albaran_data['TFNO'].strip()) == 9:
            telefono_formateado = f"+34{albaran_data['TFNO'].strip()}"
            payload["transportOptions"] = {
                "collectionOption": "CN", # "Consignor"
                "deliveryNoticeOptions": [
                    {
                        "type": "AS", # "Ankündigungsservice" (Servicio de aviso)
                        "name": albaran_data.get('NOMBRE_CONSIGNATARIO', 'Destinatario'),
                        # Usamos el mismo número para teléfono fijo y móvil
                        "phone": telefono_formateado,
                        "mobilePhone": telefono_formateado
                        # No tenemos email, así que no se añade
                    }
                ]
            }

        # La lógica ahora es: si hay pallets, se describe el envío como pallets.
        # Si no, se describe como bultos. Esto evita duplicar el peso y las líneas.

        # Línea para los pallets, incluyendo medidas si existen
        if albaran_data.get('PALLETS') and albaran_data['PALLETS'] > 0:
            linea_pallets = {
                "quantity": albaran_data['PALLETS'],
                "packaging": "EU", # Europallet
                "content": "Mercancía paletizada",
                 "weight": {
                        "weight": str(albaran_data['PESO']),
                        "unit": "KG"
                    }
            }
            
            # Añadir medidas si están disponibles
            if all(k in albaran_data for k in ('LARGO', 'ANCHO', 'ALTO')):
                linea_pallets["measure"] = {
                    "length": albaran_data['LARGO'],
                    "width": albaran_data['ANCHO'],
                    "height": albaran_data['ALTO'],
                    "unit": albaran_data.get('UD_MEDIDA', 'CM')
                }
                if 'VOLUMEN' in albaran_data:
                    linea_pallets["measure"]["volume"] = {
                        "amount": str(albaran_data.get('VOLUMEN', '0')),
                        "unit": albaran_data.get('UD_VOLUMEN', 'M3')
                    }
            
            payload['transportOrderLines'].append(linea_pallets)
        
        # Si no hay pallets, línea para los bultos/paquetes
        elif albaran_data.get('BULTOS') and albaran_data['BULTOS'] > 0:
            linea_bultos = {
                "quantity": albaran_data['BULTOS'],
                "packaging": "PC", # Package
                "content": "Mercancía general",
                "weight": {
                    "weight": str(albaran_data['PESO']),
                    "unit": "KG"
                }
            }
            # Añadir medidas si están disponibles
            if all(k in albaran_data for k in ('LARGO', 'ANCHO', 'ALTO')):
                linea_bultos["measure"] = {
                    "length": albaran_data['LARGO'],
                    "width": albaran_data['ANCHO'],
                    "height": albaran_data['ALTO'],
                    "unit": albaran_data.get('UD_MEDIDA', 'CM')
                }
                if 'VOLUMEN' in albaran_data:
                    linea_bultos["measure"]["volume"] = {
                        "amount": str(albaran_data.get('VOLUMEN', '0')),
                        "unit": albaran_data.get('UD_VOLUMEN', 'M3')
                    }
            payload['transportOrderLines'].append(linea_bultos)

    except KeyError as e:
        return {"error": f"Falta un campo esperado en los datos del albarán: {e}"}, 400
    except Exception as e:
        return {"error": f"Error al construir el payload: {e}"}, 500


    # El endpoint es /rest/v2/transportorders/{basket}
    # Usamos 'labelled' para obtener las etiquetas en la respuesta
    url_destino = f"{base_url}/rest/v2/transportorders/labelled"
    headers = {
        'X-Api-Key': api_key,
        'Content-Type': 'application/json'
    }

    try:
        print("Enviando payload a Dachser:", json.dumps(payload, indent=2))
        response = requests.post(url_destino, headers=headers, json=payload, timeout=15)
        response.raise_for_status()
        
        # --- GUARDAR EN EL LOG SI LA CREACIÓN FUE EXITOSA ---
        response_data = response.json()
        if response.status_code in [200, 201] and response_data.get('id'):
            try:
                conn_mysql_log = get_mysql_connection()
                cursor_log = conn_mysql_log.cursor()
                cursor_log.execute("INSERT INTO LOG_ENVIOS (num_albaran, id_orden_transporte) VALUES (%s, %s)", (num_albaran_actual, response_data['id']))
                conn_mysql_log.commit()
                cursor_log.close()
                conn_mysql_log.close()
            except mysql.connector.Error as log_err:
                print(f"¡ATENCIÓN! La orden {response_data['id']} se creó en Dachser pero falló al guardarse en el log: {log_err}")

        return response_data, response.status_code
    except requests.exceptions.HTTPError as e:
        # 2. Mejorar la captura de errores para mostrar el detalle
        try:
            error_details = e.response.json()
        except (ValueError, json.JSONDecodeError):
            error_details = e.response.text
        print(f"Error HTTP de la API de Dachser: {error_details}")
        return {"error": "Error de la API de Dachser", "details": error_details}, e.response.status_code
    except requests.exceptions.RequestException as e:
        print(f"Error de conexión al llamar a la API de Dachser (POST): {e}")
        return {"error": "No se pudo conectar con la API del proveedor"}, 503

# --- Rutas de la Interfaz Web ---

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/test-albaranes')
def test_albaranes_page():
    """
    Ruta de prueba para renderizar los albaranes directamente en una tabla HTML,
    sin pasar por la API de JSON ni por el JavaScript del cliente.
    Esto nos ayuda a verificar si el problema está en el backend (obtención de datos)
    o en el frontend (procesamiento con JavaScript).
    """
    print("Accediendo a la página de prueba /test-albaranes...")
    albaranes_test = get_albaranes_from_db()
    return render_template('test_albaranes.html', albaranes=albaranes_test)

# --- Rutas que actúan como API interna para el Frontend ---

@app.route('/albaranes', methods=['GET'])
def api_get_albaranes():
    """
    Endpoint API para obtener la lista de albaranes en formato JSON.
    Esta es la ruta que el JavaScript de la página principal debe consumir.
    """
    print("Accediendo a la API /albaranes...")
    albaranes = get_albaranes_from_db()
    # Si la función de base de datos devuelve None por un error, lo manejamos.
    if albaranes is None:
        return jsonify({"error": "No se pudieron obtener los albaranes del servidor."}), 500
    
    # --- Consultar el log para marcar los ya enviados ---
    try:
        conn_mysql = get_mysql_connection()
        cursor = conn_mysql.cursor()
        cursor.execute("SELECT num_albaran FROM LOG_ENVIOS")
        albaranes_enviados = {row[0] for row in cursor.fetchall()}
        cursor.close()
        conn_mysql.close()

        for albaran in albaranes:
            albaran['ENVIADO'] = albaran['NUMALB'] in albaranes_enviados
    except mysql.connector.Error as err:
        print(f"Advertencia: No se pudo consultar el log de envíos. {err}")

    return jsonify(albaranes), 200
 
@app.route('/crear-orden', methods=['POST'])
def crear_orden_transporte():
    """
    Endpoint para crear una orden de transporte en Dachser a partir de un albarán.
    """
    albaran_seleccionado = request.json
    if not albaran_seleccionado:
        return jsonify({"error": "No se recibió el albarán seleccionado"}), 400

    force_resend = request.args.get('force', 'false').lower() == 'true'
    resultado, status_code = crear_orden_dachser(albaran_seleccionado, force=force_resend)
    return jsonify(resultado), status_code

@app.route('/crear-ordenes-multiples', methods=['POST'])
def crear_ordenes_multiples():
    """
    Endpoint para crear múltiples órdenes de transporte a la vez.
    Recibe una lista de objetos de albarán.
    """
    albaranes_seleccionados = request.json
    if not isinstance(albaranes_seleccionados, list) or not albaranes_seleccionados:
        return jsonify({"error": "Se esperaba una lista de albaranes"}), 400

    force_resend = request.args.get('force', 'false').lower() == 'true'
    resultados_globales = []
    for albaran in albaranes_seleccionados:
        num_alb = albaran.get('NUMALB', 'Desconocido')
        resultado, status_code = crear_orden_dachser(albaran, force=force_resend)
        resultados_globales.append({
            "albaran": num_alb,
            "status": status_code,
            "response": resultado
        })
    return jsonify(resultados_globales), 200

@app.route('/llamar-api-etiquetas/<string:order_id>', methods=['POST'])
def proxy_api_etiquetas(order_id):
    """
    Proxy para obtener las etiquetas de una orden específica.
    Recibe el ID de la orden como parte de la URL.
    """
    base_url = os.environ.get('DACHSER_API_BASE_URL', 'https://api-gateway.dachser.com')
    api_key = os.environ.get('DACHSER_API_KEY')

    if not api_key:
         return jsonify({"error": "Falta la variable de entorno DACHSER_API_KEY"}), 500

    # Este endpoint de Dachser parece ser para obtener etiquetas, no para tracking.
    # Asumiremos que el parámetro es el ID de la orden ya creada.
    url_destino = f"{base_url}/rest/v2/transportorders/{order_id}/labels"
    headers = {'X-Api-Key': api_key}
    params = {'label-format': 'P'}

    try:
        response = requests.post(url_destino, headers=headers, params=params, timeout=10)
        response.raise_for_status()

        # La respuesta de Dachser es un JSON que contiene la etiqueta en base64
        try:
            data = response.json()
            if 'label' in data:
                # Decodificar la etiqueta de base64
                pdf_bytes = base64.b64decode(data['label'])
                
                # Devolver los bytes del PDF al navegador
                return Response(
                    pdf_bytes,
                    mimetype='application/pdf',
                    headers={'Content-Disposition': f'attachment;filename=etiqueta_{order_id}.pdf'}
                )
            else:
                # El JSON no tiene el campo 'label'
                return jsonify({"error": "La respuesta de la API no contiene el campo 'label'.", "details": data}), 500
        except ValueError:
            # La respuesta no es un JSON válido
            return jsonify({"error": "La respuesta de la API no es un JSON válido.", "details": response.text}), 500

    except requests.exceptions.HTTPError as e:
        # Si la API de Dachser devuelve un error, es probable que sea JSON
        try:
            error_details = e.response.json()
        except ValueError:
            error_details = e.response.text
        return jsonify({"error": "Error de la API de Dachser (POST)", "details": error_details}), e.response.status_code
    except requests.exceptions.RequestException as e:
        print(f"Error de conexión al llamar a la API de Dachser (POST): {e}")
        return jsonify({"error": "No se pudo conectar con la API del proveedor"}), 503


@app.route('/listar-ordenes')
def listar_ordenes_dachser():
    base_url = os.environ.get('DACHSER_API_BASE_URL', 'https://api-gateway.dachser.com')
    api_key = os.environ.get('DACHSER_API_KEY')
    date_from = request.args.get('date_from')
    date_to = request.args.get('date_to')

    if not api_key:
        return jsonify({"error": "Falta la variable de entorno DACHSER_API_KEY"}), 500
    if not all([date_from, date_to]):
        return jsonify({"error": "Faltan los parámetros de fecha"}), 400

    url_destino = f"{base_url}/rest/v2/transportorders"
    headers = {'X-Api-Key': api_key}
    params = {
        'date-from': date_from,
        'date-to': date_to
    }

    try:
        response = requests.get(url_destino, headers=headers, params=params, timeout=15)
        response.raise_for_status()
        return jsonify(response.json()), response.status_code
    except requests.exceptions.HTTPError as e:
        error_details = e.response.json() if e.response else str(e)
        return jsonify({"error": "Error de la API de Dachser (GET)", "details": error_details}), e.response.status_code
    except requests.exceptions.RequestException as e:
        print(f"Error de conexión al llamar a la API de Dachser (GET): {e}")
        return jsonify({"error": "No se pudo conectar con la API del proveedor"}), 503

@app.route('/eliminar-orden/<string:order_id>', methods=['DELETE'])
def eliminar_orden_dachser(order_id):
    base_url = os.environ.get('DACHSER_API_BASE_URL', 'https://api-gateway.dachser.com')
    api_key = os.environ.get('DACHSER_API_KEY')

    if not api_key:
        return jsonify({"error": "Falta la variable de entorno DACHSER_API_KEY"}), 500

    url_destino = f"{base_url}/rest/v2/transportorders/{order_id}"
    headers = {'X-Api-Key': api_key}

    try:
        response = requests.delete(url_destino, headers=headers, timeout=15)
        response.raise_for_status()
        # Si el borrado es exitoso, Dachser devuelve un 204 No Content, 
        # el cual no tiene cuerpo y response.json() fallaría.
        # Devolvemos nuestro propio mensaje de éxito.
        return jsonify({"mensaje": f"Orden {order_id} eliminada correctamente"}), 200
    except requests.exceptions.HTTPError as e:
        try:
            error_details = e.response.json()
        except ValueError:
            error_details = e.response.text
        return jsonify({"error": f"Error de la API de Dachser al eliminar la orden {order_id}", "details": error_details}), e.response.status_code
    except requests.exceptions.RequestException as e:
        print(f"Error de conexión al eliminar la orden en Dachser: {e}")
        return jsonify({"error": "No se pudo conectar con la API del proveedor"}), 503

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5002, debug=True)