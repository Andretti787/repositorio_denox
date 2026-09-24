"""Punto de entrada de la aplicacion DeCA (FastAPI)."""
import logging
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from .config import (ARCHIVO_ENV, DIRECTORIO_BASE, directorio_pdf, env_file_localizado,
                     rutas_env_revisadas, settings, variables_ausentes)
from .routers import albaranes

# Configuracion de logs: visibles con docker compose logs -f deca-app
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s: %(message)s",
)

logger = logging.getLogger("deca")


@asynccontextmanager
async def ciclo_de_vida(app: FastAPI):
    """Comprueba la configuracion al arrancar y prepara el directorio de PDF.

    Si faltan las variables de SQL Server queda registrado como ERROR en los logs,
    de modo que el problema se detecta al arrancar y no solo al pedir /albaranes.
    """
    directorio_pdf().mkdir(parents=True, exist_ok=True)

    if env_file_localizado():
        logger.info("Fichero de configuracion localizado: %s", ARCHIVO_ENV)
    else:
        logger.warning(
            "No se ha encontrado ningun fichero .env. Rutas revisadas: %s. La configuracion "
            "debe llegar por variables de entorno (env_file/environment en docker-compose, "
            "--env-file en docker run) o montando el fichero en una de esas rutas.",
            ", ".join(rutas_env_revisadas().keys()),
        )

    ausentes = variables_ausentes()
    if ausentes:
        logger.error(
            "FALTAN variables de conexion a SQL Server: %s. Copie .env.example a .env y rellene "
            "los valores (el .env no se versiona) o inyectelos en el contenedor. El listado de "
            "albaranes devolvera error 503 hasta que se configure.",
            ", ".join(ausentes),
        )
    else:
        logger.info(
            "Configuracion de SQL Server cargada: servidor %s, base de datos %s, usuario %s",
            settings.db_server, settings.db_database, settings.db_username,
        )
    yield


app = FastAPI(
    title="DeCA - Documento electronico de Control Administrativo",
    description="Generacion, subida y entrega del DeCA de los albaranes de transporte.",
    version="1.0.0",
    lifespan=ciclo_de_vida,
)

# La interfaz se sirve desde el mismo contenedor; se habilita CORS por si en el
# futuro se consume la API desde otro dominio corporativo.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Rutas de negocio
app.include_router(albaranes.router)


@app.get("/health", tags=["sistema"])
def estado():
    """Estado de la aplicacion y de la configuracion (sin exponer secretos)."""
    ausentes = variables_ausentes()
    return {
        "estado": "ok" if not ausentes else "configuracion_incompleta",
        "archivo_env": str(ARCHIVO_ENV),
        "env_localizado": env_file_localizado(),
        # Rutas revisadas y si existen: sirve para diagnosticar el despliegue
        "rutas_env_revisadas": rutas_env_revisadas(),
        "directorio_base": str(DIRECTORIO_BASE),
        "db_server_definido": bool(settings.db_server),
        "db_database_definida": bool(settings.db_database),
        "db_usuario_definido": bool(settings.db_username),
        "variables_ausentes": ausentes,
        "directorio_pdf": str(directorio_pdf()),
    }


# Interfaz web. Debe montarse al final para no tapar las rutas de la API.
app.mount("/", StaticFiles(directory=str(DIRECTORIO_BASE / "static"), html=True), name="static")
