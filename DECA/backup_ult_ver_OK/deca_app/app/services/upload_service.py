"""Subida del PDF al endpoint corporativo HTTPS del DeCA.

El endpoint ya existe y funciona con multipart/form-data (campo "pdf") y
devuelve un JSON con la URL final de descarga directa: {"url": "https://..."}.
"""
import logging
import ssl
from urllib.parse import urlparse

import httpx

from ..config import settings

logger = logging.getLogger("deca.upload")


class ErrorSubida(RuntimeError):
    """Error controlado al subir el PDF al endpoint externo."""


def _contexto_tls() -> ssl.SSLContext:
    """Contexto TLS que exige TLS 1.2 o superior y verifica el certificado."""
    contexto = ssl.create_default_context()
    contexto.minimum_version = ssl.TLSVersion.TLSv1_2
    return contexto


def _validar_url_descarga(url: str) -> None:
    """La URL devuelta debe ser HTTPS de descarga directa y sin credenciales."""
    partes = urlparse(url)
    if partes.scheme != "https":
        raise ErrorSubida(f"La URL devuelta no es HTTPS: {url}")
    if partes.username or partes.password:
        raise ErrorSubida("La URL devuelta contiene credenciales; debe ser de descarga directa")


def subir_pdf(pdf_bytes: bytes, nombre_fichero: str) -> str:
    """Sube el PDF por HTTPS y devuelve la URL publica de descarga directa.

    Se envia el campo opcional "filename" para que el servidor utilice el
    nombre sugerido: asi la URL final coincide con la que ya contiene el QR
    incrustado en el PDF.

    Raises:
        ErrorSubida: si falla la red, el endpoint devuelve error o la URL no es valida.
    """
    url_endpoint = settings.deca_upload_url
    if not url_endpoint.lower().startswith("https://"):
        raise ErrorSubida(f"La URL de subida debe usar HTTPS: {url_endpoint}")

    try:
        with httpx.Client(verify=_contexto_tls(), timeout=30.0) as cliente:
            respuesta = cliente.post(
                url_endpoint,
                files={"pdf": (nombre_fichero, pdf_bytes, "application/pdf")},
                data={"filename": nombre_fichero},
            )
    except httpx.HTTPError as exc:
        logger.error("Fallo de red al subir el DeCA: %s", exc)
        raise ErrorSubida(f"No se pudo conectar con el endpoint de subida: {exc}") from exc

    if respuesta.status_code != 200:
        logger.error("El endpoint devolvio HTTP %s: %s", respuesta.status_code, respuesta.text[:500])
        raise ErrorSubida(f"El endpoint de subida devolvio HTTP {respuesta.status_code}")

    try:
        url = respuesta.json()["url"]
    except (ValueError, KeyError) as exc:
        logger.error("Respuesta inesperada del endpoint: %s", respuesta.text[:200])
        raise ErrorSubida("Respuesta inesperada del endpoint de subida") from exc

    _validar_url_descarga(url)
    logger.info("DeCA subido correctamente: %s", url)
    return url