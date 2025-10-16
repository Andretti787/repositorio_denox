import os
import pyodbc # Descomentar cuando las credenciales reales se configuren
import socket # Importamos la librería socket
import time   # Importamos time para medir la duración

# --- NOTA ---
# Esta es una función de ejemplo. Debes configurar tus variables de entorno
# (en un fichero .env) y descomentar el código real para conectar a SQL Server.

def get_albaranes_from_db():
    """
    Establece conexión con SQL Server y obtiene los albaranes.
    """
    albaranes = []
    try:
         # Lee las credenciales desde las variables de entorno
         server = os.environ.get('DB_SERVER')
         database = os.environ.get('DB_DATABASE')
         username = os.environ.get('DB_USERNAME')
         password = os.environ.get('DB_PASSWORD')
         driver = os.environ.get('DB_DRIVER', '{ODBC Driver 17 for SQL Server}')

         print("\n--- PASO 1: Verificando variables de entorno ---")
         if not all([server, database, username, password]):
             print("-> ERROR: Faltan variables de entorno para la conexión a la base de datos.")
             return []
         print(f"-> Servidor: {server}, Base de datos: {database}, Usuario: {username}")

         print("\n--- PASO 2: Probando conectividad de red (socket) ---")
         check_server_connectivity(server, 1433)

         print("\n--- PASO 3: Intentando conectar con pyodbc (timeout de 5s) ---")
         connection_string = f"DRIVER={driver};SERVER={server};DATABASE={database};UID={username};PWD={password};Connection Timeout=5"
        
         start_time = time.time()
         with pyodbc.connect(connection_string, timeout=5) as conn:
             end_time = time.time()
             print(f"-> Conexión con pyodbc exitosa en {end_time - start_time:.2f} segundos.")

             cursor = conn.cursor()
             
             print("\n--- PASO 4: Configurando LOCK_TIMEOUT en la consulta (10s) ---")
             cursor.execute("SET LOCK_TIMEOUT 10000") # 10000 milisegundos = 10 segundos
             print("-> LOCK_TIMEOUT configurado a 10 segundos.")

             print("\n--- PASO 5: Ejecutando la consulta SQL ---")
             sql_query = """
            -- Convertimos la fecha a formato YYYY-MM-DD directamente en la consulta
            SELECT TOP 100 '72761617' AS ID_CLIENTE, BPDNAM_0 AS NOMBRE_CONSIGNATARIO, SUBSTRING(CONCAT(BPDADDLIG_0, BPDADDLIG_1, BPDADDLIG_2),1,30) AS DIR_CONSIG
            , SUBSTRING(CONCAT(BPDADDLIG_0, BPDADDLIG_1, BPDADDLIG_2),30,60) AS DIR_CONSIG2
            , BPDCTY_0 AS CIUDAD, BPDPOSCOD_0 AS COD_POSTAL, BPDCRY_0 AS PAIS
            , SDHNUM_0 AS NUMALB, CONVERT(varchar, SHIDAT_0, 23) AS FECHA_ENVIO, PTE_0, PACNBR_0 AS PALLETS, YPACK_0 AS BULTOS, GROWEI_0 AS PESO, 'KG' AS UD_PESO
            , 180 AS LARGO, 90 AS ANCHO, 120 AS ALTO, 'CM' AS UD_MEDIDA
            FROM LIVE.SDELIVERY
            WHERE BPTNUM_0 = '025'
            ORDER BY SHIDAT_0 DESC
             """
             print("-> Ejecutando: " + " ".join(sql_query.split()))
             
             start_time_query = time.time()
             cursor.execute(sql_query)
             end_time_query = time.time()
             print(f"-> La ejecución de la consulta tardó {end_time_query - start_time_query:.2f} segundos.")

             print("\n--- PASO 6: Recuperando resultados (fetchall) ---")
             start_time_fetch = time.time()
             rows = cursor.fetchall()
             end_time_fetch = time.time()
             print(f"-> fetchall() tardó {end_time_fetch - start_time_fetch:.2f} segundos.")

             columns = [column[0] for column in cursor.description]
             # CONVERSIÓN EXPLÍCITA PARA GARANTIZAR COMPATIBILIDAD JSON
             for row in rows:
                 # Creamos un diccionario, pero nos aseguramos de que los tipos de datos
                 # sean compatibles con JSON (str, int, float, bool, None).
                 # El tipo Decimal de la BD no siempre es serializable.
                 row_dict = dict(zip(columns, row))
                 row_dict['PESO'] = float(row_dict['PESO']) if row_dict.get('PESO') is not None else 0.0
                 albaranes.append(row_dict)
             
             print(f"\n¡ÉXITO! Se han recuperado {len(albaranes)} registros.")

    except (pyodbc.Error, socket.error, ValueError) as ex: # Capturamos también los errores de socket y de valor
         print(f"\n*** ERROR DURANTE LA OBTENCIÓN DE DATOS ***")
         print(f"-> Tipo de error: {type(ex).__name__}")
         print(f"-> Mensaje: {ex}")
         return None # Devuelve None para indicar un error
        
    return albaranes

def check_server_connectivity(server: str, port: int, timeout: int = 3):
    """
    Verifica si un servidor es accesible en un puerto específico.
    Maneja formatos de servidor como 'IP,PUERTO' o 'SERVIDOR\\INSTANCIA'.
    Lanza un error si no puede conectar.
    """
    server_address = server
    target_port = port

    # Si el servidor contiene una coma, es formato 'IP,PUERTO'
    if ',' in server:
        parts = server.split(',')
        server_address = parts[0].strip()
        try:
            target_port = int(parts[1].strip())
        except (ValueError, IndexError):
            # Si el formato es incorrecto, lanzamos un error claro.
            raise ValueError(f"Formato de servidor inválido. Se esperaba 'IP,PUERTO' pero se recibió '{server}'")
    else:
        # Si no hay coma, podría ser 'SERVIDOR\INSTANCIA', solo usamos la parte del servidor.
        server_address = server.split('\\')[0] # Aquí '\\' es correcto porque está en el código, no en un comentario.

    try:
        print(f"Intentando verificar conectividad con {server_address} en el puerto {target_port}...")
        with socket.create_connection((server_address, target_port), timeout=timeout):
            print(f"-> Conectividad con {server_address}:{target_port} verificada con éxito.")
    except socket.gaierror as e:
        # Error de resolución de nombre (DNS)
        raise socket.error(f"No se pudo resolver el nombre del servidor: '{server_address}'. Verifica que el nombre es correcto y accesible desde tu red.") from e
    except (socket.timeout, ConnectionRefusedError) as e:
        # Error de timeout o conexión rechazada
        raise socket.error(f"El servidor '{server_address}' no está accesible en el puerto {target_port}. Verifica que el servidor esté encendido y que no haya un firewall bloqueando la conexión.") from e
