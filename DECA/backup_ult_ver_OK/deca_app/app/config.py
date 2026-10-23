"""Configuracion central de la aplicacion DeCA.

La configuracion se obtiene, por orden de prioridad:

1. Variables de entorno reales del proceso (las que inyecta Docker con env_file,
   environment o docker run --env-file). Son las que manda en produccion.
2. Primer fichero .env encontrado entre varias rutas candidatas (ver RUTAS_ENV).

Nunca se hardcodean secretos en el codigo.
"""
import os
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

# Directorio raiz del proyecto (deca_app). Calculado desde __file__, de modo que no
# depende del directorio desde el que se arranque la aplicacion. En el contenedor
# Docker equivale a /app porque el Dockerfile usa WORKDIR /app.
DIRECTORIO_BASE = Path(__file__).resolve().parent.parent


def _rutas_candidatas() -> list[Path]:
    """Rutas donde se busca el fichero .env, en orden de preferencia.

    Contempla los despliegues habituales: ruta forzada por DECA_ENV_FILE, raiz del
    proyecto (/app/.env en el contenedor), directorio de trabajo actual, un nivel
    por encima (si el codigo se copio en /app/deca_app) y los secretos de Docker.
    """
    candidatas: list[Path] = []
    forzada = os.environ.get('DECA_ENV_FILE')
    if forzada:
        candidatas.append(Path(forzada))
    candidatas.extend([
        DIRECTORIO_BASE / '.env',
        Path.cwd() / '.env',
        DIRECTORIO_BASE.parent / '.env',
        Path('/run/secrets/deca.env'),
    ])

    unicas: list[Path] = []
    for ruta in candidatas:
        if ruta not in unicas:
            unicas.append(ruta)
    return unicas


RUTAS_ENV = _rutas_candidatas()
# Primer fichero que exista; si no hay ninguno se conserva la ruta principal para
# poder informar de ella en los logs y en /health.
ARCHIVO_ENV = next((ruta for ruta in RUTAS_ENV if ruta.is_file()), RUTAS_ENV[0])

# Variables imprescindibles para conectar con SQL Server
VARIABLES_OBLIGATORIAS = ('DB_SERVER', 'DB_DATABASE', 'DB_USERNAME', 'DB_PASSWORD')


class Settings(BaseSettings):
    """Parametros de configuracion leidos desde variables de entorno."""

    model_config = SettingsConfigDict(
        env_file=ARCHIVO_ENV,
        env_file_encoding='utf-8',
        extra='ignore',
    )

    # --- Conexion a SQL Server (Sage X3) ---
    db_server: str = ''
    db_database: str = ''
    db_username: str = ''
    db_password: str = ''
    db_driver: str = '{ODBC Driver 17 for SQL Server}'
    # Segundos de espera al conectar con SQL Server (subirlo si la red es lenta)
    db_timeout: int = 5

    # --- Datos del cargador contractual (fallback si la BD no los aporta) ---
    # Preferencia: LIVE.FACILITY + LIVE.BPADDRESS. Estas variables solo se usan
    # cuando el NIF o el domicilio del sitio de carga vienen vacios.
    deca_cargador_nombre: str = ''
    deca_cargador_nif: str = ''
    deca_cargador_domicilio: str = ''


    # --- Endpoint externo de subida del DeCA ---
    deca_upload_url: str = 'https://denox.eu/DECA/upload_deca.php'
    # URL publica base de los PDF. Debe coincidir con la variable baseUrl del PHP
    deca_base_url: str = 'https://denox.eu/DECA/deca_storage/'

    # --- SMTP para el envio del DeCA al conductor (opcional) ---
    smtp_host: str = ''
    smtp_port: int = 465
    smtp_user: str = ''
    smtp_password: str = ''
    smtp_from: str = 'deca@denox.eu'

    # --- Aplicacion ---
    app_port: int = 5010
    # Directorio local con copia de los PDF generados
    pdf_dir: str = 'deca_pdf'
    # Limite normativo de tamano del PDF (5 MB)
    pdf_max_bytes: int = 5 * 1024 * 1024
    # Numero maximo de albaranes que se pueden pedir de una vez al listado
    albaranes_limite_max: int = 1000


def variables_ausentes() -> list[str]:
    """Devuelve las variables obligatorias que no estan definidas."""
    valores = {
        'DB_SERVER': settings.db_server,
        'DB_DATABASE': settings.db_database,
        'DB_USERNAME': settings.db_username,
        'DB_PASSWORD': settings.db_password,
    }
    return [nombre for nombre, valor in valores.items() if not valor]


def env_file_localizado() -> bool:
    """Indica si se ha encontrado algun fichero .env en las rutas candidatas."""
    return ARCHIVO_ENV.is_file()


def rutas_env_revisadas() -> dict:
    """Rutas candidatas y si existen: permite diagnosticar el despliegue."""
    return {str(ruta): ruta.is_file() for ruta in RUTAS_ENV}


def directorio_pdf() -> Path:
    """Ruta absoluta del directorio local de PDF.

    Si PDF_DIR es relativo se resuelve respecto a la raiz del proyecto, para que no
    dependa del directorio desde el que se arranque la aplicacion.
    """
    ruta = Path(settings.pdf_dir)
    return ruta if ruta.is_absolute() else DIRECTORIO_BASE / ruta


# Instancia unica de configuracion reutilizada por todos los modulos
settings = Settings()
