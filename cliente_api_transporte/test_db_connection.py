# test_db_connection.py
import os
from dotenv import load_dotenv
import pyodbc
import socket
import time

# Cargar las mismas variables de entorno que usa tu aplicación
load_dotenv()

def check_server_connectivity(server: str, port: int, timeout: int = 3):
    """
    Verifica si un servidor es accesible en un puerto específico.
    Maneja formatos de servidor como 'IP,PUERTO' o 'SERVIDOR\INSTANCIA'.
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
            raise ValueError(f"Formato de servidor inválido. Se esperaba 'IP,PUERTO' pero se recibió '{server}'")
    else:
        # Si no hay coma, podría ser 'SERVIDOR\INSTANCIA', solo usamos la parte del servidor.
        server_address = server.split('\\')[0]

    try:
        print(f"Intentando verificar conectividad con {server_address} en el puerto {target_port}...")
        with socket.create_connection((server_address, target_port), timeout=timeout):
            print(f"-> Conectividad con {server_address}:{target_port} verificada con éxito.")
    except Exception as e:
        print(f"-> FALLO la prueba de conectividad: {e}")
        raise

def get_albaranes_test():
    """Función de prueba para conectar y consultar la base de datos."""
    try:
        # 1. Leer credenciales
        server = os.environ.get('DB_SERVER')
        database = os.environ.get('DB_DATABASE')
        username = os.environ.get('DB_USERNAME')
        password = os.environ.get('DB_PASSWORD')
        driver = os.environ.get('DB_DRIVER', '{ODBC Driver 17 for SQL Server}')
        
        if not all([server, database, username, password]):
            print("ERROR: Faltan variables de entorno. Asegúrate de que el fichero .env está en este directorio.")
            return

        print("\n--- PASO 1: Verificando variables de entorno ---")
        print(f"Servidor: {server}, Base de datos: {database}, Usuario: {username}")

        # 2. Prueba de conectividad de red
        print("\n--- PASO 2: Probando conectividad de red (socket) ---")
        check_server_connectivity(server, 1433) # El puerto 1433 es solo un default, la función usará el del string si existe.

        # 3. Intento de conexión con pyodbc
        print("\n--- PASO 3: Intentando conectar con pyodbc (timeout de 5s) ---")
        connection_string = f"DRIVER={driver};SERVER={server};DATABASE={database};UID={username};PWD={password};Connection Timeout=5"
        
        start_time = time.time()
        with pyodbc.connect(connection_string, timeout=5) as conn:
            end_time = time.time()
            print(f"-> Conexión con pyodbc exitosa en {end_time - start_time:.2f} segundos.")
            
            cursor = conn.cursor()
            
            # 4. Configurar timeout de la consulta
            print("\n--- PASO 4: Configurando LOCK_TIMEOUT en la consulta (10s) ---")
            cursor.execute("SET LOCK_TIMEOUT 10000")
            print("-> LOCK_TIMEOUT configurado a 10 segundos.")

            # 5. Ejecutar la consulta
            print("\n--- PASO 5: Ejecutando la consulta SQL ---")
            sql_query = """
               SELECT TOP 100 '72761617' AS ID_CLIENTE, BPDNAM_0 AS NOMBRE_CONSIGNATARIO, SUBSTRING(CONCAT(BPDADDLIG_0, BPDADDLIG_1, BPDADDLIG_2),1,30) AS DIR_CONSIG
            , SUBSTRING(CONCAT(BPDADDLIG_0, BPDADDLIG_1, BPDADDLIG_2),30,60) AS DIR_CONSIG2
            , BPDCTY_0 AS CIUDAD, BPDPOSCOD_0 AS COD_POSTAL, BPDCRY_0 AS PAIS
            , SDHNUM_0 AS NUMALB, SHIDAT_0 AS FECHA_ENVIO, PTE_0, PACNBR_0 AS PALLETS, YPACK_0 AS BULTOS, GROWEI_0 AS PESO, 'KG' AS UD_PESO
            , 180 AS LARGO, 90 AS ANCHO, 120 AS ALTO, 'CM' AS UD_MEDIDA
            fROM LIVE.SDELIVERY
            WHERE BPTNUM_0 = '025'
            ORDER BY SHIDAT_0 DESC
            """
            print("Ejecutando: " + " ".join(sql_query.split()))
            
            start_time = time.time()
            cursor.execute(sql_query)
            end_time = time.time()
            print(f"-> La ejecución de la consulta tardó {end_time - start_time:.2f} segundos.")

            # 6. Recuperar los resultados
            print("\n--- PASO 6: Recuperando resultados (fetchall) ---")
            start_time = time.time()
            rows = cursor.fetchall()
            end_time = time.time()
            print(f"-> fetchall() tardó {end_time - start_time:.2f} segundos.")
            print(f"\n¡ÉXITO! Se han recuperado {len(rows)} registros.")

    except Exception as e:
        print(f"\n*** ERROR INESPERADO DURANTE LA PRUEBA ***")
        print(f"Tipo de error: {type(e).__name__}")
        print(f"Mensaje: {e}")

if __name__ == "__main__":
    get_albaranes_test()
