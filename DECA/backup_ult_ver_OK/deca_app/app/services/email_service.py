"""Envio del DeCA por correo electronico al conductor (opcional).

Requiere configurar las variables SMTP_* en el entorno. El envio usa
smtplib.SMTP_SSL (TLS implicito) con el contexto TLS por defecto de Python
(TLS 1.2 o superior).
"""
import logging
import smtplib
from email.message import EmailMessage

from ..config import settings

logger = logging.getLogger("deca.email")


class ErrorEnvioEmail(RuntimeError):
    """Error controlado en el envio del correo con el DeCA."""


def enviar_pdf_por_email(destinatario: str, numalbaran: str, pdf_bytes: bytes, nombre_fichero: str) -> None:
    """Envia el PDF del DeCA como adjunto al correo indicado.

    Raises:
        ErrorEnvioEmail: si el SMTP no esta configurado o falla el envio.
    """
    if not settings.smtp_host:
        raise ErrorEnvioEmail("El envio por email no esta configurado (faltan las variables SMTP_*)")

    mensaje = EmailMessage()
    mensaje["From"] = settings.smtp_from
    mensaje["To"] = destinatario
    mensaje["Subject"] = f"DeCA del albaran {numalbaran}"
    mensaje.set_content(
        "Adjunto se remite el Documento electronico de Control Administrativo (DeCA) "
        f"correspondiente al albaran {numalbaran}.\n\n"
        "Documento generado electronicamente conforme a la Resolucion de 5 de junio de 2026, "
        "la disposicion transitoria octava de la Ley de Movilidad Sostenible y la Orden FOM/2861/2012.\n"
    )
    mensaje.add_attachment(
        pdf_bytes,
        maintype="application",
        subtype="pdf",
        filename=nombre_fichero,
    )

    try:
        with smtplib.SMTP_SSL(settings.smtp_host, settings.smtp_port, timeout=20) as servidor:
            if settings.smtp_user:
                servidor.login(settings.smtp_user, settings.smtp_password)
            servidor.send_message(mensaje)
    except (smtplib.SMTPException, OSError) as exc:
        logger.error("Error al enviar el DeCA por email a %s: %s", destinatario, exc)
        raise ErrorEnvioEmail(f"No se pudo enviar el correo: {exc}") from exc

    logger.info("DeCA del albaran %s enviado por email a %s", numalbaran, destinatario)