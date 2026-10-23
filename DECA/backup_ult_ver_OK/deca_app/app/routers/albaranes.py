"""Endpoints de la API REST de albaranes y generacion del DeCA."""
import logging
from datetime import date
from pathlib import Path

import pyodbc
from fastapi import APIRouter, HTTPException
from fastapi.responses import FileResponse

from .. import database
from ..config import directorio_pdf, settings, variables_ausentes
from ..models import (
    AlbaranOut,
    DecaManualRequest,
    DecaMultiRequest,
    DecaResponse,
    EmailRequest,
    MatriculasUpdate,
    OrigenDetalle,
)
from ..services import email_service, pdf_service, upload_service
from ..services.pdf_service import formatear_destino

logger = logging.getLogger("deca.api")
router = APIRouter(prefix="/albaranes", tags=["albaranes"])


def _traducir_error_sql(exc: pyodbc.Error) -> tuple[int, str]:
    ausentes = variables_ausentes()
    if ausentes:
        return 503, (
            "Faltan las variables de configuracion de SQL Server: "
            + " y ".join(ausentes)
            + ". Copie .env.example a .env y rellene los valores."
        )
    argumentos = getattr(exc, "args", ())
    sqlstate = str(argumentos[0]) if argumentos else ""
    detalle = str(argumentos[1]) if len(argumentos) > 1 else str(exc)
    for prefijo in (
        "[Microsoft][ODBC Driver 17 for SQL Server][SQL Server]",
        "[Microsoft][ODBC Driver 18 for SQL Server][SQL Server]",
    ):
        detalle = detalle.replace(prefijo, "")
    detalle = detalle.split("(SQLExecDirectW)")[0].strip()
    if sqlstate.startswith("08") or sqlstate in ("HYT00", "HYT01"):
        destino = (
            f"{settings.db_server or 'DB_SERVER sin definir'}/"
            f"{settings.db_database or 'DB_DATABASE sin definir'}"
        )
        return 503, f"No se pudo conectar con SQL Server ({destino}): {detalle[:200]}"

    # Columna inexistente: casi siempre falta ejecutar la migracion de LIVE.ZDECA
    if sqlstate == "42S22" and any(
        columna in detalle.upper()
        for columna in (
            "ZDESTINO_0",
            "ZOBSERV_0",
            "ZTRANSP_0",
            "ZTRANSNIF_0",
            "ZORIGEN_0",
            "ZFECHA_0",
            "ZPESO_0",
            "ZPALETS_0",
            "ZMATTRA_0",
            "ZMATREM_0",
            "ZDECAURL_0",
        )
    ):
        return 500, (
            f"Error de SQL Server ({sqlstate}): {detalle[:300]}. "
            "Falta una columna en LIVE.ZDECA: ejecute el script sql/create_zdeca.sql "
            "sobre la base de datos para crearla."
        )
    return 500, f"Error de SQL Server ({sqlstate}): {detalle[:300]}"


def _directorio_pdf() -> Path:
    ruta = directorio_pdf()
    ruta.mkdir(parents=True, exist_ok=True)
    return ruta


def _nombre_desde_url(url: str) -> str:
    return url.rstrip("/").rsplit("/", 1)[-1]


def _obtener_albaran_o_404(numalbaran: str) -> dict:
    try:
        albaran = database.obtener_albaran(numalbaran)
    except pyodbc.Error as exc:
        logger.error("Error SQL al recuperar %s: %s", numalbaran, exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc
    if albaran is None:
        raise HTTPException(status_code=404, detail=f"Albaran {numalbaran} no encontrado")
    return albaran


def _apilar_destinos(albaranes: list[dict]) -> list[str]:
    destinos: list[str] = []
    vistos: set[str] = set()
    for a in albaranes:
        texto = formatear_destino(a)
        if texto and texto not in vistos:
            vistos.add(texto)
            destinos.append(texto)
    return destinos


def _apilar_observaciones(albaranes: list[dict]) -> str:
    """Une las observaciones editadas de los albaranes consolidados.

    El PDF imprime un unico campo de observaciones, por lo que se concatenan
    (sin repetir) las de cada albaran seleccionado.
    """
    return _unir_distintos(albaranes, "observaciones", "; ")


def _unir_distintos(albaranes: list[dict], campo: str, separador: str = "; ") -> str:
    """Une los valores distintos de un campo (sin repetir) en un solo texto."""
    partes: list[str] = []
    vistos: set[str] = set()
    for a in albaranes:
        texto = str(a.get(campo) or "").strip()
        if texto and texto not in vistos:
            vistos.add(texto)
            partes.append(texto)
    return separador.join(partes)


def _consolidar_albaranes(albaranes: list[dict]) -> dict:
    peso_total = sum(float(a.get("peso_kg") or 0) for a in albaranes)
    palets_totales = sum(int(float(a.get("num_pallets") or 0)) for a in albaranes)
    destinos = _apilar_destinos(albaranes)
    p = albaranes[0]
    return {
        "numalbaran": ", ".join(a["numalbaran"] for a in albaranes),
        "fecha_transporte": p.get("fecha_transporte") or "",
        "cargador_nombre": (p.get("cargador_nombre") or p.get("origen") or ""),
        "cargador_nif": p.get("cargador_nif") or "",
        "cargador_domicilio": p.get("cargador_domicilio") or "",
        # Si los albaranes seleccionados son de transportistas distintos se listan todos
        "transportista_nombre": _unir_distintos(albaranes, "transportista_nombre", " / "),
        "transportista_nif": _unir_distintos(albaranes, "transportista_nif", " / "),
        "origen": p.get("origen") or "",
        "destino_ciudad": p.get("destino_ciudad") or "",
        "destino_cp": p.get("destino_cp") or "",
        "destino_pais": p.get("destino_pais") or "",
        # Destino efectivo del primer albaran (manual o CP + ciudad)
        "destino": p.get("destino") or "",
        "destino_apilado": "\n".join(destinos),
        # La naturaleza depende del cargador; se listan todas si hay varias
        "naturaleza_mercancia": _unir_distintos(albaranes, "naturaleza_mercancia", " / ")
        or database.NATURALEZA_FIJA,
        "peso_kg": peso_total,
        "num_pallets": palets_totales,
        "autorizacion_especial": p.get("autorizacion_especial") or "",
        "observaciones": _apilar_observaciones(albaranes),
        "matricula_tractor": p.get("matricula_tractor"),
        "matricula_remolque": p.get("matricula_remolque"),
    }


@router.get("", response_model=list[AlbaranOut])
def listar_albaranes(
    solo_pendientes: bool = True,
    limite: int = 200,
    desplazamiento: int = 0,
    filtro: str = "",
    origen: str = "",
    transportista: str = "",
    ordenar_por: str = "fecha_transporte",
    orden_desc: bool = True,
):
    try:
        return database.listar_albaranes(
            solo_pendientes=solo_pendientes,
            limite=limite,
            desplazamiento=desplazamiento,
            filtro=filtro,
            origen=origen,
            transportista=transportista,
            ordenar_por=ordenar_por,
            orden_desc=orden_desc,
        )
    except pyodbc.Error as exc:
        logger.error("Error SQL al listar albaranes: %s", exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc


@router.get("/origenes", response_model=list[str])
def listar_origenes(solo_pendientes: bool = False):
    """Lista de origenes distintos para el filtro de pantalla."""
    try:
        return database.listar_origenes(solo_pendientes=solo_pendientes)
    except pyodbc.Error as exc:
        logger.error("Error SQL al listar origenes: %s", exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc


@router.get("/origenes-detalle", response_model=list[OrigenDetalle])
def listar_origenes_detalle():
    """Origenes con los datos del cargador contractual (para el DeCA sin albaran)."""
    try:
        return database.listar_origenes_detalle()
    except pyodbc.Error as exc:
        logger.error("Error SQL al listar origenes con detalle: %s", exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc


@router.post("/deca-manual", response_model=DecaResponse)
def generar_deca_manual(datos: DecaManualRequest):
    """Genera un DeCA sin albaran.

    Se rellenan los campos editables en pantalla y los datos del cargador
    contractual se arrastran del origen seleccionado (nombre, NIF, domicilio y
    naturaleza de la mercancia). El documento se sube al endpoint y se registra
    en LIVE.ZDECA con una referencia propia.
    """
    try:
        cargador = database.obtener_cargador(origen=datos.origen)
    except pyodbc.Error as exc:
        logger.error("Error SQL al resolver el cargador del origen %s: %s", datos.origen, exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc

    if not cargador:
        raise HTTPException(
            status_code=404,
            detail=f"No se encontro el cargador del origen '{datos.origen}'",
        )

    referencia = database.generar_referencia_manual()
    albaran = {
        "numalbaran": referencia,
        "fecha_transporte": (datos.fecha_transporte or "").strip()
        or date.today().isoformat(),
        "cargador_nombre": cargador.get("cargador_nombre") or "",
        "cargador_nif": cargador.get("cargador_nif") or "",
        "cargador_domicilio": cargador.get("cargador_domicilio") or "",
        "transportista_nombre": (datos.transportista_nombre or "").strip(),
        "transportista_nif": (datos.transportista_nif or "").strip(),
        "origen": cargador.get("origen") or datos.origen,
        "destino": (datos.destino or "").strip(),
        "destino_ciudad": "",
        "destino_cp": "",
        "destino_pais": "",
        "naturaleza_mercancia": cargador.get("naturaleza_mercancia")
        or database.NATURALEZA_FIJA,
        "peso_kg": float(datos.peso_kg or 0),
        "num_pallets": int(datos.num_pallets or 0),
        "autorizacion_especial": "",
        "observaciones": (datos.observaciones or "").strip(),
        "matricula_tractor": datos.matricula_tractor,
        "matricula_remolque": datos.matricula_remolque,
    }

    try:
        pdf_bytes, nombre_fichero, url_prevista = pdf_service.generar_pdf_deca(albaran)
    except RuntimeError as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Error inesperado generando el PDF del DeCA sin albaran")
        raise HTTPException(
            status_code=500, detail=f"Error al generar el PDF: {exc}"
        ) from exc

    (_directorio_pdf() / nombre_fichero).write_bytes(pdf_bytes)

    try:
        url_final = upload_service.subir_pdf(pdf_bytes, nombre_fichero)
    except upload_service.ErrorSubida as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc

    if url_final != url_prevista:
        logger.warning("La URL devuelta (%s) difiere de la del QR (%s)", url_final, url_prevista)

    try:
        database.guardar_deca_manual(referencia, albaran, url_final)
    except pyodbc.Error as exc:
        logger.critical(
            "El DeCA sin albaran se subio (%s) pero no se pudo registrar: %s", url_final, exc
        )
        _, mensaje = _traducir_error_sql(exc)
        raise HTTPException(
            status_code=500,
            detail=f"PDF subido correctamente ({url_final}) pero no se pudo registrar en la base de datos: {mensaje}",
        ) from exc

    logger.info("DeCA sin albaran %s generado: %s", referencia, url_final)
    return DecaResponse(
        numalbaran=referencia,
        url=url_final,
        estado=1,
        mensaje=f"DeCA sin albaran generado y subido correctamente ({referencia})",
    )


@router.put("/{numalbaran}", response_model=AlbaranOut)
def actualizar_matriculas(numalbaran: str, datos: MatriculasUpdate):
    _obtener_albaran_o_404(numalbaran)
    try:
        database.guardar_matriculas(numalbaran, datos.model_dump())
        return database.obtener_albaran(numalbaran)
    except pyodbc.Error as exc:
        logger.error("Error SQL al guardar matriculas de %s: %s", numalbaran, exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc


@router.post("/deca-multi", response_model=DecaResponse)
def generar_deca_multiple(datos: DecaMultiRequest):
    """Genera un unico DeCA consolidado para varios albaranes.

    Suma pesos y palets, apila destinos con CP delante, naturaleza fija
    MENAJE DE PLASTICO y sin destinatario. Marca todos como generados.
    """
    try:
        albaranes = database.obtener_multiplos_albaranes(datos.numalbaranes)
    except pyodbc.Error as exc:
        logger.error("Error SQL al obtener albaranes multi: %s", exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc

    if not albaranes:
        raise HTTPException(
            status_code=404,
            detail="Ninguno de los albaranes seleccionados existe",
        )

    existentes = {a["numalbaran"] for a in albaranes}
    faltan = [n for n in datos.numalbaranes if n not in existentes]
    if faltan:
        raise HTTPException(
            status_code=404,
            detail="Albaranes no encontrados: " + ", ".join(faltan),
        )

    sin_matricula = [
        a["numalbaran"]
        for a in albaranes
        if not (a.get("matricula_tractor") or "").strip()
    ]
    if sin_matricula:
        raise HTTPException(
            status_code=400,
            detail="Falta la matricula del vehiculo tractor en: "
            + ", ".join(sin_matricula),
        )

    consolidado = _consolidar_albaranes(albaranes)
    logger.info(
        "Consolidado multi: %d albaranes, peso=%.2f, palets=%s, destinos=%s",
        len(albaranes),
        consolidado["peso_kg"],
        consolidado["num_pallets"],
        consolidado["destino_apilado"].replace("\n", " | "),
    )

    try:
        pdf_bytes, nombre_fichero, url_prevista = pdf_service.generar_pdf_deca_multi(
            consolidado
        )
    except RuntimeError as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Error inesperado generando PDF multi")
        raise HTTPException(
            status_code=500, detail=f"Error al generar el PDF: {exc}"
        ) from exc

    (_directorio_pdf() / nombre_fichero).write_bytes(pdf_bytes)

    try:
        url_final = upload_service.subir_pdf(pdf_bytes, nombre_fichero)
    except upload_service.ErrorSubida as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc

    if url_final != url_prevista:
        logger.warning(
            "URL devuelta (%s) difiere de la del QR (%s)", url_final, url_prevista
        )

    try:
        for a in albaranes:
            database.guardar_deca_generado(a["numalbaran"], url_final)
    except pyodbc.Error as exc:
        logger.error("Error al marcar albaranes multi: %s", exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc

    n_destinos = len(_apilar_destinos(albaranes))
    return DecaResponse(
        numalbaran=" + ".join(a["numalbaran"] for a in albaranes),
        url=url_final,
        estado=1,
        mensaje=(
            f"DeCA consolidado generado para {len(albaranes)} albaranes "
            f"(peso total {consolidado['peso_kg']:.2f} kg, "
            f"{consolidado['num_pallets']} palets, {n_destinos} destino(s))"
        ),
    )


@router.post("/{numalbaran}/deca", response_model=DecaResponse)
def generar_deca(numalbaran: str):
    albaran = _obtener_albaran_o_404(numalbaran)
    if not (albaran.get("matricula_tractor") or "").strip():
        raise HTTPException(
            status_code=400,
            detail="Debe informar la matricula del vehiculo tractor antes de generar el DeCA",
        )
    try:
        pdf_bytes, nombre_fichero, url_prevista = pdf_service.generar_pdf_deca(albaran)
    except RuntimeError as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Error inesperado generando PDF de %s", numalbaran)
        raise HTTPException(status_code=500, detail=f"Error al generar el PDF: {exc}") from exc

    (_directorio_pdf() / nombre_fichero).write_bytes(pdf_bytes)
    try:
        url_final = upload_service.subir_pdf(pdf_bytes, nombre_fichero)
    except upload_service.ErrorSubida as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc

    if url_final != url_prevista:
        logger.warning("URL devuelta (%s) difiere de la del QR (%s)", url_final, url_prevista)

    try:
        database.guardar_deca_generado(numalbaran, url_final)
    except pyodbc.Error as exc:
        logger.error("Error al guardar URL DeCA de %s: %s", numalbaran, exc)
        codigo, mensaje = _traducir_error_sql(exc)
        raise HTTPException(status_code=codigo, detail=mensaje) from exc

    return DecaResponse(
        numalbaran=numalbaran,
        url=url_final,
        estado=1,
        mensaje="DeCA generado y subido correctamente",
    )


@router.get("/{numalbaran}/deca/pdf")
def descargar_pdf(numalbaran: str):
    albaran = _obtener_albaran_o_404(numalbaran)
    url = albaran.get("deca_url")
    if not url:
        raise HTTPException(status_code=404, detail="El albaran aun no tiene DeCA generado")
    nombre_fichero = _nombre_desde_url(url)
    ruta = _directorio_pdf() / nombre_fichero
    if not ruta.is_file():
        raise HTTPException(
            status_code=404,
            detail="PDF local no disponible; use la URL publica del DeCA",
        )
    return FileResponse(ruta, media_type="application/pdf", filename=nombre_fichero)


@router.post("/{numalbaran}/deca/email")
def enviar_deca_por_email(numalbaran: str, datos: EmailRequest):
    albaran = _obtener_albaran_o_404(numalbaran)
    url = albaran.get("deca_url")
    if not url:
        raise HTTPException(status_code=400, detail="Genere primero el DeCA del albaran")
    nombre_fichero = _nombre_desde_url(url)
    ruta = _directorio_pdf() / nombre_fichero
    if not ruta.is_file():
        raise HTTPException(status_code=404, detail="PDF local no disponible")
    try:
        email_service.enviar_pdf_por_email(
            datos.email, numalbaran, ruta.read_bytes(), nombre_fichero
        )
    except email_service.ErrorEnvioEmail as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc
    return {"mensaje": f"DeCA del albaran {numalbaran} enviado a {datos.email}"}


