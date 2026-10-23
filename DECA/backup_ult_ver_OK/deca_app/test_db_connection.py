"""Diagnostico de la conexion a SQL Server y del estado de la base de datos.

Devuelve codigo de salida 0 si todo es correcto y 1 si hay algun fallo.

Uso dentro del contenedor:
    docker compose exec deca-app python test_db_connection.py
Uso en local (dependencias instaladas y fichero .env en la carpeta del proyecto):
    python test_db_connection.py
"""
import socket
import sys
import time

import pyodbc

from app.config import settings


def comprobar_configuracion() -> bool:
    """Comprueba que las variables de entorno imprescindibles estan definidas."""
    print("--- Configuracion ---")
    print("  DB_SERVER   :", settings.db_server or "(sin definir)")
    print("  DB_DATABASE :", settings.db_database or "(sin definir)")
    print("  DB_USERNAME :", settings.db_username or "(sin definir)")
    print("  DB_PASSWORD :", "definida" if settings.db_password else "(sin definir)")
    print("  DB_DRIVER   :", settings.db_driver)
    print("  DB_TIMEOUT  :", settings.db_timeout, "s")

    faltan = [
        nombre
        for nombre, valor in (
            ("DB_SERVER", settings.db_server),
            ("DB_DATABASE", settings.db_database),
            ("DB_USERNAME", settings.db_username),
            ("DB_PASSWORD", settings.db_password),
        )
        if not valor
    ]
    if faltan:
        print("  FALTAN variables:", ", ".join(faltan))
        return False
    return True


def comprobar_red() -> bool:
    """Comprueba la conectividad TCP al servidor (independiente de las credenciales)."""
    print("--- Red ---")
    servidor = settings.db_server
    puerto = 1433
    if "," in servidor:
        partes = servidor.split(",")
        servidor = partes[0].strip()
        try:
            puerto = int(partes[1].strip())
        except (IndexError, ValueError):
            print("  Formato de DB_SERVER no reconocido (se espera IP,PUERTO)")
            return False
    elif chr(92) in servidor:
        servidor = servidor.split(chr(92))[0]  # se ignora la instancia (SERVIDOR\INSTANCIA)

    try:
        inicio = time.time()
        with socket.create_connection((servidor, puerto), timeout=settings.db_timeout):
            print("  " + servidor + ":" + str(puerto) + " accesible en " + str(round(time.time() - inicio, 2)) + " s")
        return True
    except OSError as error:
        print("  ERROR al abrir " + servidor + ":" + str(puerto) + " -> " + str(error))
        print("  Revise VPN/firewall, que el puerto de la instancia sea el correcto y que el servidor este encendido.")
        return False


def comprobar_login() -> bool:
    """Intenta conectar con credenciales y ejecuta consultas basicas."""
    print("--- Conexion (login) ---")
    cadena = (
        "DRIVER=" + settings.db_driver + ";SERVER=" + settings.db_server +
        ";DATABASE=" + settings.db_database + ";UID=" + settings.db_username +
        ";PWD=" + settings.db_password + ";Connection Timeout=" + str(settings.db_timeout) + ";"
    )
    try:
        inicio = time.time()
        conexion = pyodbc.connect(cadena, timeout=settings.db_timeout)
    except pyodbc.Error as error:
        sqlstate = str(error.args[0]) if error.args else ""
        detalle = error.args[1] if len(error.args) > 1 else str(error)
        print("  ERROR (" + sqlstate + "): " + str(detalle)[:300])
        if sqlstate.startswith("08") or sqlstate in ("HYT00", "HYT01"):
            print("  -> Fallo de red o de tiempo de espera al conectar.")
            print("     Si la red es lenta, aumente DB_TIMEOUT en el fichero .env y reinicie la aplicacion.")
        elif sqlstate in ("28000", "18456"):
            print("  -> Credenciales incorrectas (revise DB_USERNAME / DB_PASSWORD).")
        else:
            print("  -> Revise DB_DRIVER (driver ODBC instalado) y el nombre de la base de datos.")
        return False

    print("  Conexion establecida en " + str(round(time.time() - inicio, 2)) + " s")
    with conexion:
        cursor = conexion.cursor()
        cursor.execute("SELECT DB_NAME(), SUSER_NAME(), @@VERSION")
        base, usuario, version = cursor.fetchone()
        print("  Base de datos:", base, "| usuario:", usuario)
        print("  Motor        :", str(version).splitlines()[0][:80])

        print("--- Datos ---")
        cursor.execute("SELECT COUNT(*) FROM LIVE.SDELIVERY")
        print("  Albaranes en LIVE.SDELIVERY     :", cursor.fetchone()[0])
        cursor.execute(
            "SELECT COUNT(*) FROM LIVE.SDELIVERY DEL "
            "LEFT JOIN LIVE.ZDECA Z ON Z.SDHNUM_0 = DEL.SDHNUM_0 "
            "WHERE ISNULL(Z.ZDECAEST_0, 0) = 0"
        )
        print("  Albaranes pendientes de DeCA    :", cursor.fetchone()[0])
        cursor.execute("SELECT COUNT(*) FROM LIVE.ZDECA WHERE ZDECAEST_0 = 1")
        print("  DeCA ya generados (LIVE.ZDECA)  :", cursor.fetchone()[0])
    return True


def main() -> int:
    print("=== Diagnostico DeCA -> SQL Server ===")
    if not comprobar_configuracion():
        print("RESULTADO: configuracion incompleta. Revise el fichero .env")
        return 1
    if not comprobar_red():
        print("RESULTADO: el servidor SQL no es accesible desde este equipo/contenedor")
        return 1
    if not comprobar_login():
        print("RESULTADO: no se pudo completar el login en SQL Server")
        return 1
    print("RESULTADO: conexion y datos correctos")
    return 0


if __name__ == "__main__":
    sys.exit(main())
