"""Generacion del PDF nativo del DeCA con codigo QR y acentos correctos."""
import io
import logging
import uuid
from datetime import datetime
from pathlib import Path
from xml.sax.saxutils import escape

import qrcode
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Image, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

from ..config import settings

logger = logging.getLogger("deca.pdf")

NATURALEZA_FIJA = "MENAJE DE PLÁSTICO"

_FUENTES_REG = [
    Path(r"C:\Windows\Fonts\arial.ttf"),
    Path(r"C:\Windows\Fonts\calibri.ttf"),
    Path("/usr/share/fonts/dejavu-sans-fonts/DejaVuSans.ttf"),
    Path("/usr/share/fonts/dejavu/DejaVuSans.ttf"),
    Path("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
    Path("/usr/share/fonts/liberation/LiberationSans-Regular.ttf"),
    Path("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"),
]
_FUENTES_BOLD = [
    Path(r"C:\Windows\Fonts\arialbd.ttf"),
    Path(r"C:\Windows\Fonts\calibrib.ttf"),
    Path("/usr/share/fonts/dejavu-sans-fonts/DejaVuSans-Bold.ttf"),
    Path("/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf"),
    Path("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"),
    Path("/usr/share/fonts/liberation/LiberationSans-Bold.ttf"),
    Path("/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"),
]

_FUENTE_REG = "Helvetica"
_FUENTE_BOLD = "Helvetica-Bold"
_FUENTES_OK = False


def _registrar_fuentes() -> None:
    """Registra fuente TTF con glifos latinos (acentos y ene)."""
    global _FUENTE_REG, _FUENTE_BOLD, _FUENTES_OK
    if _FUENTES_OK:
        return
    reg = next((p for p in _FUENTES_REG if p.is_file()), None)
    bold = next((p for p in _FUENTES_BOLD if p.is_file()), None)
    try:
        if reg is not None:
            pdfmetrics.registerFont(TTFont("DeCASans", str(reg)))
            _FUENTE_REG = "DeCASans"
            logger.info("Fuente PDF Unicode: %s", reg)
        if bold is not None:
            pdfmetrics.registerFont(TTFont("DeCASans-Bold", str(bold)))
            _FUENTE_BOLD = "DeCASans-Bold"
        elif reg is not None:
            _FUENTE_BOLD = "DeCASans"
        if reg is None:
            logger.warning("Sin fuente TrueType Unicode; instale dejavu-sans-fonts.")
    except Exception as exc:
        logger.warning("No se registraron fuentes TTF (%s)", exc)
        _FUENTE_REG = "Helvetica"
        _FUENTE_BOLD = "Helvetica-Bold"
    _FUENTES_OK = True


def generar_nombre_pdf(numalbaran: str) -> str:
    identificador = uuid.uuid4().hex[:12]
    seguro = "".join(c for c in numalbaran if c.isalnum())
    return f"DECA_{seguro}_{identificador}.pdf"


def generar_nombre_pdf_multi(referencia: str) -> str:
    identificador = uuid.uuid4().hex[:12]
    seguro = "".join(c for c in referencia if c.isalnum())[:24] or "VARIOS"
    return f"DECA_MULTI_{seguro}_{identificador}.pdf"


def url_publica(nombre_fichero: str) -> str:
    return settings.deca_base_url.rstrip("/") + "/" + nombre_fichero


def _imagen_qr(url: str, lado_mm: int = 38) -> Image:
    codigo = qrcode.QRCode(border=1, box_size=10, error_correction=qrcode.constants.ERROR_CORRECT_M)
    codigo.add_data(url)
    codigo.make(fit=True)
    buffer = io.BytesIO()
    codigo.make_image(fill_color="black", back_color="white").save(buffer, format="PNG")
    buffer.seek(0)
    return Image(buffer, width=lado_mm * mm, height=lado_mm * mm)


def _valor(albaran: dict, clave: str, defecto: str = "-") -> str:
    valor = albaran.get(clave)
    if valor is None or str(valor).strip() == "":
        return defecto
    return escape(str(valor))


def formatear_destino(albaran: dict) -> str:
    cp = str(albaran.get("destino_cp") or "").strip()
    ciudad = str(albaran.get("destino_ciudad") or "").strip()
    pais = str(albaran.get("destino_pais") or "").strip()
    partes = []
    if cp:
        partes.append(cp)
    if ciudad:
        partes.append(ciudad)
    if pais:
        partes.append(f"({pais})")
    return " ".join(partes).strip()


def _estilos():
    _registrar_fuentes()
    base = getSampleStyleSheet()
    return {
        "titulo": ParagraphStyle(
            "titulo", parent=base["Title"], fontName=_FUENTE_BOLD,
            fontSize=14, leading=18, spaceAfter=4,
        ),
        "subtitulo": ParagraphStyle(
            "subtitulo", parent=base["Normal"], fontName=_FUENTE_REG,
            fontSize=8, leading=10, textColor=colors.grey, spaceAfter=8,
        ),
        "celda": ParagraphStyle(
            "celda", parent=base["Normal"], fontName=_FUENTE_REG,
            fontSize=9, leading=12,
        ),
        "celda_b": ParagraphStyle(
            "celda_b", parent=base["Normal"], fontName=_FUENTE_BOLD,
            fontSize=9, leading=12,
        ),
        "firma": ParagraphStyle(
            "firma", parent=base["Normal"], fontName=_FUENTE_REG,
            fontSize=8, leading=11, alignment=1,
        ),
        "pie": ParagraphStyle(
            "pie", parent=base["Normal"], fontName=_FUENTE_REG,
            fontSize=7, leading=9, textColor=colors.grey,
        ),
    }


def _construir_pdf(titulo: str, campos: list[tuple[str, str]], url: str) -> bytes:
    estilos = _estilos()
    buffer = io.BytesIO()
    documento = SimpleDocTemplate(
        buffer,
        pagesize=A4,
        leftMargin=18 * mm,
        rightMargin=18 * mm,
        topMargin=16 * mm,
        bottomMargin=16 * mm,
        title=titulo,
        author="DeCA - denox.eu",
        subject="Documento electrónico de Control Administrativo",
        creator="deca_app",
    )

    filas = [
        [Paragraph(escape(etiqueta), estilos["celda_b"]), Paragraph(valor, estilos["celda"])]
        for etiqueta, valor in campos
    ]
    tabla_datos = Table(filas, colWidths=[70 * mm, 104 * mm])
    tabla_datos.setStyle(TableStyle([
        ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
        ("BACKGROUND", (0, 0), (0, -1), colors.whitesmoke),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))

    caja_cargador = [
        Paragraph("<b>Firma del cargador</b>", estilos["firma"]),
        Spacer(1, 18 * mm),
        Paragraph("Nombre y fecha: _______________________", estilos["firma"]),
    ]
    caja_transportista = [
        Paragraph("<b>Firma del transportista</b>", estilos["firma"]),
        Spacer(1, 18 * mm),
        Paragraph("Nombre y fecha: _______________________", estilos["firma"]),
    ]
    tabla_firmas = Table(
        [[caja_cargador, caja_transportista]],
        colWidths=[87 * mm, 87 * mm],
        rowHeights=[42 * mm],
    )
    tabla_firmas.setStyle(TableStyle([
        ("BOX", (0, 0), (0, 0), 0.8, colors.black),
        ("BOX", (1, 0), (1, 0), 0.8, colors.black),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
        ("BACKGROUND", (0, 0), (-1, -1), colors.Color(0.98, 0.98, 0.98)),
    ]))

    texto_qr = Paragraph(
        "<b>Verificación electrónica del documento</b><br/><br/>"
        "Escanee el código QR para descargar el DeCA en formato PDF.<br/>"
        f"<font size='7'>{escape(url)}</font>",
        estilos["celda"],
    )
    tabla_qr = Table([[_imagen_qr(url), texto_qr]], colWidths=[45 * mm, 129 * mm])
    tabla_qr.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "MIDDLE")]))

    momento = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
    pie = Paragraph(
        "Documento electrónico de Control Administrativo (DeCA) generado el "
        f"{momento}, conforme a la Resolución de 5 de junio de 2026, la "
        "disposición transitoria octava de la Ley de Movilidad Sostenible y la Orden "
        "FOM/2861/2012. Documento nativo digital (no escaneado). Verificable en la "
        "URL indicada anteriormente.",
        estilos["pie"],
    )

    historia = [
        Paragraph(escape(titulo), estilos["titulo"]),
        Paragraph(
            "Resolución de 5 de junio de 2026 · DT 8.ª Ley de Movilidad Sostenible "
            "· Orden FOM/2861/2012",
            estilos["subtitulo"],
        ),
        tabla_datos,
        Spacer(1, 6 * mm),
        tabla_firmas,
        Spacer(1, 6 * mm),
        tabla_qr,
        Spacer(1, 4 * mm),
        pie,
    ]
    documento.build(historia)

    pdf_bytes = buffer.getvalue()
    if len(pdf_bytes) > settings.pdf_max_bytes:
        raise RuntimeError(
            f"El PDF generado ({len(pdf_bytes)} bytes) supera el limite de 5 MB"
        )
    return pdf_bytes


def _campos_comunes(
    albaran: dict, destino_html: str, peso_texto: str, palets_texto: str
) -> list[tuple[str, str]]:
    # Etiquetas con acentos correctos (fuente Unicode las renderiza)
    return [
        ("Número de albarán", _valor(albaran, "numalbaran")),
        ("Fecha de realización del transporte", _valor(albaran, "fecha_transporte")),
        ("Cargador contractual", _valor(albaran, "cargador_nombre")),
        ("NIF del cargador contractual", _valor(albaran, "cargador_nif")),
        ("Domicilio del cargador contractual", _valor(albaran, "cargador_domicilio")),
        ("Transportista efectivo", _valor(albaran, "transportista_nombre")),
        ("NIF del transportista efectivo", _valor(albaran, "transportista_nif")),
        ("Origen / lugar de carga", _valor(albaran, "origen")),
        ("Lugar de destino", destino_html),
        ("Naturaleza de la mercancía", escape(NATURALEZA_FIJA)),
        ("Peso de la mercancía", peso_texto),
        ("Número de palets", palets_texto),
        (
            "Autorización especial de circulación",
            _valor(albaran, "autorizacion_especial", "No aplica"),
        ),
        ("Matrícula del vehículo tractor", _valor(albaran, "matricula_tractor")),
        ("Matrícula del remolque", _valor(albaran, "matricula_remolque", "No aplica")),
        ("Observaciones", _valor(albaran, "observaciones")),
    ]


def generar_pdf_deca(albaran: dict) -> tuple[bytes, str, str]:
    nombre_fichero = generar_nombre_pdf(str(albaran.get("numalbaran") or "ALB"))
    url = url_publica(nombre_fichero)
    destino = escape(formatear_destino(albaran)) or "-"
    try:
        peso = float(albaran.get("peso_kg") or 0)
        peso_texto = (
            f"{peso:,.2f} kg".replace(",", "X").replace(".", ",").replace("X", ".")
        )
    except (TypeError, ValueError):
        peso_texto = f"{_valor(albaran, 'peso_kg')} kg"
    try:
        palets = int(float(albaran.get("num_pallets") or 0))
    except (TypeError, ValueError):
        palets = 0

    campos = _campos_comunes(albaran, destino, peso_texto, str(palets))
    pdf_bytes = _construir_pdf(
        "DOCUMENTO ELECTRÓNICO DE CONTROL ADMINISTRATIVO (DeCA)",
        campos,
        url,
    )
    logger.info("PDF DeCA generado: %s (%d bytes)", nombre_fichero, len(pdf_bytes))
    return pdf_bytes, nombre_fichero, url


def generar_pdf_deca_multi(albaran_consolidado: dict) -> tuple[bytes, str, str]:
    ref = str(albaran_consolidado.get("numalbaran") or "VARIOS")
    nombre_fichero = generar_nombre_pdf_multi(ref.split(",")[0].strip() or "VARIOS")
    url = url_publica(nombre_fichero)

    bruto = str(albaran_consolidado.get("destino_apilado") or "").strip()
    if bruto:
        lineas = [
            escape(l.strip())
            for l in bruto.replace("<br/>", "\n").split("\n")
            if l.strip()
        ]
        destino_html = "<br/>".join(lineas) if lineas else "-"
    else:
        destino_html = escape(formatear_destino(albaran_consolidado)) or "-"

    try:
        peso = float(albaran_consolidado.get("peso_kg") or 0)
        peso_texto = (
            f"{peso:,.2f} kg (total)"
            .replace(",", "X")
            .replace(".", ",")
            .replace("X", ".")
        )
    except (TypeError, ValueError):
        peso_texto = f"{_valor(albaran_consolidado, 'peso_kg')} kg (total)"
    try:
        palets = int(float(albaran_consolidado.get("num_pallets") or 0))
    except (TypeError, ValueError):
        palets = 0

    campos = _campos_comunes(
        albaran_consolidado, destino_html, peso_texto, f"{palets} (total)"
    )
    pdf_bytes = _construir_pdf(
        "DOCUMENTO ELECTRÓNICO DE CONTROL ADMINISTRATIVO (DeCA) - MULTI-ALBARÁN",
        campos,
        url,
    )
    logger.info("PDF DeCA multi generado: %s (%d bytes)", nombre_fichero, len(pdf_bytes))
    return pdf_bytes, nombre_fichero, url


