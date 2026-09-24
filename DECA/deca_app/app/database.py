"""Capa de acceso a datos: SQL Server (Sage X3) mediante pyodbc.

Mapeo de campos (esquema real comprobado en la base de datos x3):
  - Albaran:            LIVE.SDELIVERY
                        (SDHNUM_0, SHIDAT_0, GROWEI_0, PACNBR_0, STOFCY_0, BPTNUM_0,
                         BPDADDLIG_0/1/2, BPDCTY_0, BPDPOSCOD_0, BPDCRYNAM_0/BPDCRY_0)
  - Cliente:            LIVE.BPCUSTOMER (solo para busqueda/filtro)
  - Cargador/empresa:   LIVE.FACILITY (FCYNAM_0, CRN_0) + LIVE.BPADDRESS (domicilio)
  - Transportista:      LIVE.BPARTNER (BPRNAM_0, CRN_0) + LIVE.BPCARRIER.BPTNAM_0
  - Observaciones:      LIVE.BPDLVCUST.DLVTEX_0 -> LIVE.TEXCLOB.TEXTE_0
  - Campos editables:   LIVE.ZDECA (matriculas, URL y estado del DeCA)

La naturaleza de la mercancia es un valor fijo ("MENAJE DE PLASTICO"): no se
consultan las lineas de albaran (SDELIVERYD/ITMMASTER).
"""
import logging
import re
from contextlib import contextmanager
from datetime import date, datetime
from decimal import Decimal

import pyodbc

from .config import settings

logger = logging.getLogger("deca.db")

# Pool de conexiones integrado en pyodbc
pyodbc.pooling = True

NATURALEZA_FIJA = "MENAJE DE PLÁSTICO"


def _cadena_conexion() -> str:
    """Construye la cadena de conexion ODBC desde las variables de entorno."""
    return (
        f"DRIVER={settings.db_driver};"
        f"SERVER={settings.db_server};"
        f"DATABASE={settings.db_database};"
        f"UID={settings.db_username};"
        f"PWD={settings.db_password};"
        f"Connection Timeout={settings.db_timeout};"
    )


@contextmanager
def get_connection():
    """Devuelve una conexion del pool y la libera al salir del contexto."""
    conexion = None
    try:
        conexion = pyodbc.connect(_cadena_conexion(), timeout=settings.db_timeout)
        yield conexion
    except pyodbc.Error as exc:
        logger.error("Error de conexion o consulta a SQL Server: %s", exc)
        raise
    finally:
        if conexion is not None:
            conexion.close()


# Expresion SQL del origen / lugar de carga: CP + ciudad del cargador (BPADDRESS)
_SQL_ORIGEN = (
    "NULLIF(LTRIM(RTRIM(CONCAT("
    "NULLIF(LTRIM(RTRIM(CAST(ADR.POSCOD_0 AS NVARCHAR(20)))), ''), "
    "N' ', "
    "NULLIF(LTRIM(RTRIM(CAST(ADR.CTY_0 AS NVARCHAR(40)))), '')"
    "))), '')"
)
_SQL_BASE = """
SELECT
    DEL.SDHNUM_0                       AS numalbaran,
    CONVERT(varchar, DEL.SHIDAT_0, 23) AS fecha_transporte,
    COALESCE(NULLIF(LTRIM(RTRIM(FCY.FCYNAM_0)), ''), DEL.STOFCY_0) AS cargador_nombre,
    NULLIF(LTRIM(RTRIM(CAST(FCY.CRN_0 AS NVARCHAR(40)))), '') AS cargador_nif,
    NULLIF(LTRIM(RTRIM(
        CONCAT(
            NULLIF(LTRIM(RTRIM(ADR.BPAADDLIG_0)), ''), N' ',
            NULLIF(LTRIM(RTRIM(ADR.BPAADDLIG_1)), ''), N' ',
            NULLIF(LTRIM(RTRIM(ADR.POSCOD_0)), ''), N' ',
            NULLIF(LTRIM(RTRIM(ADR.CTY_0)), ''), N' ',
            NULLIF(LTRIM(RTRIM(ADR.CRYNAM_0)), '')
        )
    )), '')                            AS cargador_domicilio,
    COALESCE(
        NULLIF(LTRIM(RTRIM(TPT.BPRNAM_0)), ''),
        NULLIF(LTRIM(RTRIM(CPT.BPTNAM_0)), ''),
        DEL.BPTNUM_0
    )                                  AS transportista_nombre,
    DEL.BPTNUM_0                       AS transportista_codigo,
    TPT.CRN_0                          AS transportista_nif,
    {ORIGEN_EXPR} AS origen,
    DEL.BPDCTY_0                       AS destino_ciudad,
    DEL.BPDPOSCOD_0                    AS destino_cp,
    COALESCE(NULLIF(LTRIM(RTRIM(DEL.BPDCRYNAM_0)), ''), DEL.BPDCRY_0) AS destino_pais,
    CAST('' AS NVARCHAR(240))          AS naturaleza_mercancia,
    COALESCE(Z.ZPESO_0, DEL.GROWEI_0)  AS peso_kg,
    CAST(COALESCE(Z.ZPALETS_0, DEL.PACNBR_0) AS FLOAT) AS num_pallets,
    CAST('' AS NVARCHAR(60))           AS autorizacion_especial,
    CASE SUBSTRING(OBS.TEXTE_0, 1, 1)
        WHEN '{' THEN ''
        ELSE SUBSTRING(REPLACE(REPLACE(OBS.TEXTE_0, CHAR(13), ' '), CHAR(10), ' '), 1, 149)
    END                                AS observaciones,
    COALESCE(NULLIF(LTRIM(RTRIM(Z.ZMATTRA_0)), ''), MAT.ZMATTRA_0) AS matricula_tractor,
    COALESCE(NULLIF(LTRIM(RTRIM(Z.ZMATREM_0)), ''), MAT.ZMATREM_0) AS matricula_remolque,
    Z.ZDECAURL_0                       AS deca_url,
    ISNULL(Z.ZDECAEST_0, 0)            AS deca_estado
FROM LIVE.SDELIVERY DEL
LEFT JOIN LIVE.BPCUSTOMER CLI ON CLI.BPCNUM_0 = DEL.BPCORD_0
LEFT JOIN LIVE.BPARTNER TPT   ON TPT.BPRNUM_0 = DEL.BPTNUM_0
LEFT JOIN LIVE.BPCARRIER CPT  ON CPT.BPTNUM_0 = DEL.BPTNUM_0
LEFT JOIN LIVE.FACILITY FCY   ON FCY.FCY_0 = DEL.STOFCY_0
OUTER APPLY (
    SELECT TOP 1
        A.BPAADDLIG_0, A.BPAADDLIG_1, A.POSCOD_0, A.CTY_0, A.CRYNAM_0
    FROM LIVE.BPADDRESS A
    WHERE A.BPANUM_0 = DEL.STOFCY_0
      AND A.BPATYP_0 = 3
) ADR
LEFT JOIN LIVE.ZDECA Z        ON Z.SDHNUM_0 = DEL.SDHNUM_0
LEFT JOIN LIVE.ZDECA_MATRICULA MAT ON MAT.BPTNUM_0 = DEL.BPTNUM_0
OUTER APPLY (
    SELECT TOP 1 TEX.TEXTE_0
    FROM LIVE.BPDLVCUST BPD
    LEFT JOIN LIVE.TEXCLOB TEX ON TEX.CODE_0 = BPD.DLVTEX_0
    WHERE BPD.BPCNUM_0 = DEL.BPCORD_0
) OBS
"""
_SQL_BASE = _SQL_BASE.replace("{ORIGEN_EXPR}", _SQL_ORIGEN)




def _serializar(valor):
    """Convierte tipos de SQL Server a tipos JSON-serializables."""
    if valor is None:
        return None
    if isinstance(valor, Decimal):
        return float(valor)
    if isinstance(valor, datetime):
        # DATETIME con hora (CREDAT_0 / UPDDAT_0)
        return valor.strftime("%Y-%m-%d %H:%M:%S")
    if isinstance(valor, date):
        return valor.isoformat()
    if isinstance(valor, bytes):
        return valor.decode("utf-8", errors="replace")
    return valor


def _limpiar_texto(valor) -> str:
    """Normaliza espacios multiples y recorta extremos."""
    if valor is None:
        return ""
    return re.sub(r"\s+", " ", str(valor)).strip()


def _fila_a_dict(cursor, fila) -> dict:
    """Convierte una fila pyodbc en diccionario con tipos serializables."""
    columnas = [col[0] for col in cursor.description]
    datos = {col: _serializar(valor) for col, valor in zip(columnas, fila)}

    # Cargador contractual desde BD (FACILITY + BPADDRESS).
    # Fallback al .env solo si la BD no aporta el dato.
    origen = _limpiar_texto(datos.get("origen"))
    cargador_nombre = _limpiar_texto(datos.get("cargador_nombre")) or origen
    cargador_nif = _limpiar_texto(datos.get("cargador_nif")) or (
        settings.deca_cargador_nif or ""
    )
    cargador_domicilio = _limpiar_texto(datos.get("cargador_domicilio")) or (
        settings.deca_cargador_domicilio or ""
    )

    datos["origen"] = origen
    datos["cargador_nombre"] = cargador_nombre
    datos["cargador_nif"] = cargador_nif
    datos["cargador_domicilio"] = cargador_domicilio
    datos["naturaleza_mercancia"] = NATURALEZA_FIJA
    # Normalizar palets a entero y peso a float
    try:
        datos["num_pallets"] = int(float(datos.get("num_pallets") or 0))
    except (TypeError, ValueError):
        datos["num_pallets"] = 0
    try:
        datos["peso_kg"] = float(datos.get("peso_kg") or 0)
    except (TypeError, ValueError):
        datos["peso_kg"] = 0.0
    return datos


def _deduplicar_por_albaran(filas: list[dict]) -> list[dict]:
    """Garantiza una sola fila por numalbaran (conserva el primer encuentro)."""
    vistos: set[str] = set()
    resultado: list[dict] = []
    for fila in filas:
        clave = fila.get("numalbaran")
        if not clave or clave in vistos:
            continue
        vistos.add(clave)
        resultado.append(fila)
    if len(resultado) != len(filas):
        logger.warning(
            "Se eliminaron %d filas duplicadas del listado de albaranes",
            len(filas) - len(resultado),
        )
    return resultado


def listar_albaranes(
    solo_pendientes: bool = True,
    limite: int = 200,
    desplazamiento: int = 0,
    filtro: str = "",
    origen: str = "",
    transportista: str = "",
    ordenar_por: str = "fecha_transporte",
    orden_desc: bool = True,
) -> list[dict]:
    """Lista albaranes con paginacion, filtros y ordenacion."""
    limite = max(1, min(int(limite or 200), settings.albaranes_limite_max))
    desplazamiento = max(0, int(desplazamiento or 0))

    # Whitelist de ordenacion. numalbaran usa solo SDHNUM_0 (evita error SQL 169)
    columnas_orden = {
        "numalbaran": "DEL.SDHNUM_0",
        "fecha_transporte": "DEL.SHIDAT_0",
        "transportista_nombre": (
            "COALESCE(NULLIF(LTRIM(RTRIM(TPT.BPRNAM_0)), ''), "
            "NULLIF(LTRIM(RTRIM(CPT.BPTNAM_0)), ''), DEL.BPTNUM_0)"
        ),
        "origen": _SQL_ORIGEN,
        "destino_ciudad": "DEL.BPDCTY_0",
        "destino_cp": "DEL.BPDPOSCOD_0",
        "peso_kg": "COALESCE(Z.ZPESO_0, DEL.GROWEI_0)",
        "num_pallets": "COALESCE(Z.ZPALETS_0, DEL.PACNBR_0)",
        "matricula_tractor": "COALESCE(NULLIF(LTRIM(RTRIM(Z.ZMATTRA_0)), ''), MAT.ZMATTRA_0)",
        "matricula_remolque": "COALESCE(NULLIF(LTRIM(RTRIM(Z.ZMATREM_0)), ''), MAT.ZMATREM_0)",
        "deca_estado": "ISNULL(Z.ZDECAEST_0, 0)",
    }
    col_orden = columnas_orden.get(
        (ordenar_por or "").strip(), columnas_orden["fecha_transporte"]
    )
    sentido = "DESC" if orden_desc else "ASC"
    if col_orden.strip().upper() == "DEL.SDHNUM_0":
        sql_orden = f" ORDER BY DEL.SDHNUM_0 {sentido}"
    else:
        sql_orden = f" ORDER BY {col_orden} {sentido}, DEL.SDHNUM_0 DESC"

    condiciones = []
    parametros: list = []

    if solo_pendientes:
        condiciones.append("ISNULL(Z.ZDECAEST_0, 0) = 0")

    origen = (origen or "").strip()
    if origen:
        condiciones.append(_SQL_ORIGEN + " = ?")
        parametros.append(origen)

    transportista = (transportista or "").strip()
    if transportista:
        comodin_t = f"%{transportista}%"
        condiciones.append(
            "(COALESCE(NULLIF(LTRIM(RTRIM(TPT.BPRNAM_0)), ''), "
            "NULLIF(LTRIM(RTRIM(CPT.BPTNAM_0)), ''), DEL.BPTNUM_0) LIKE ? "
            "OR DEL.BPTNUM_0 LIKE ? OR TPT.CRN_0 LIKE ?)"
        )
        parametros.extend([comodin_t, comodin_t, comodin_t])

    filtro = (filtro or "").strip()
    if filtro:
        comodin = f"%{filtro}%"
        condiciones.append(
            "(DEL.SDHNUM_0 LIKE ? OR CLI.BPCNAM_0 LIKE ? OR DEL.BPDCTY_0 LIKE ? OR "
            + _SQL_ORIGEN + " LIKE ?)"
        )
        parametros.extend([comodin, comodin, comodin, comodin])

    sql = _SQL_BASE
    if condiciones:
        sql += " WHERE " + " AND ".join(condiciones)
    sql += sql_orden
    sql += " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"
    parametros.extend([desplazamiento, limite])

    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute("SET LOCK_TIMEOUT 10000")
        cursor.execute(sql, parametros)
        return _deduplicar_por_albaran(
            [_fila_a_dict(cursor, fila) for fila in cursor.fetchall()]
        )


def listar_origenes(solo_pendientes: bool = False) -> list[str]:
    """Devuelve los origenes distintos (CP + ciudad) para el listbox de filtro."""
    sql = f"""
SELECT DISTINCT
    {_SQL_ORIGEN} AS origen
FROM LIVE.SDELIVERY DEL
OUTER APPLY (
    SELECT TOP 1 A.POSCOD_0, A.CTY_0
    FROM LIVE.BPADDRESS A
    WHERE A.BPANUM_0 = DEL.STOFCY_0
      AND A.BPATYP_0 = 3
) ADR
LEFT JOIN LIVE.ZDECA Z ON Z.SDHNUM_0 = DEL.SDHNUM_0
WHERE {_SQL_ORIGEN} IS NOT NULL
"""
    if solo_pendientes:
        sql += " AND ISNULL(Z.ZDECAEST_0, 0) = 0"
    sql += " ORDER BY 1"
    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute("SET LOCK_TIMEOUT 10000")
        cursor.execute(sql)
        return [str(fila[0]).strip() for fila in cursor.fetchall() if fila and fila[0]]


def obtener_albaran(numalbaran: str) -> dict | None:
    """Devuelve un albaran concreto o None si no existe."""
    sql = _SQL_BASE + " WHERE DEL.SDHNUM_0 = ?"
    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute("SET LOCK_TIMEOUT 10000")
        cursor.execute(sql, numalbaran)
        filas = [_fila_a_dict(cursor, fila) for fila in cursor.fetchall()]
    unicos = _deduplicar_por_albaran(filas)
    return unicos[0] if unicos else None


def obtener_multiplos_albaranes(numalbaranes: list[str]) -> list[dict]:
    """Devuelve la lista de albaranes solicitados preservando el orden pedido."""
    if not numalbaranes:
        return []

    vistos = set()
    unicos = []
    for n in numalbaranes:
        n = (n or "").strip()
        if n and n not in vistos:
            vistos.add(n)
            unicos.append(n)

    if not unicos:
        return []

    marcadores = ", ".join("?" for _ in unicos)
    sql = _SQL_BASE + f" WHERE DEL.SDHNUM_0 IN ({marcadores})"
    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute("SET LOCK_TIMEOUT 10000")
        cursor.execute(sql, unicos)
        mapa: dict[str, dict] = {}
        for fila in cursor.fetchall():
            d = _fila_a_dict(cursor, fila)
            # Si hubiera duplicados por JOIN, nos quedamos con el primero
            if d["numalbaran"] not in mapa:
                mapa[d["numalbaran"]] = d

    return [mapa[n] for n in unicos if n in mapa]


def guardar_matriculas(numalbaran: str, matriculas: dict) -> None:
    """UPSERT en LIVE.ZDECA de matriculas, peso y palets editables.

    Si el albaran tiene transportista, tambien actualiza LIVE.ZDECA_MATRICULA
    para que futuros albaranes del mismo transportista hereden las matriculas.
    """
    sql = """
MERGE LIVE.ZDECA AS destino
USING (SELECT ? AS SDHNUM_0, ? AS ZMATTRA_0, ? AS ZMATREM_0,
              ? AS ZPESO_0, ? AS ZPALETS_0) AS origen
   ON destino.SDHNUM_0 = origen.SDHNUM_0
WHEN MATCHED THEN
    UPDATE SET ZMATTRA_0 = origen.ZMATTRA_0,
               ZMATREM_0 = origen.ZMATREM_0,
               ZPESO_0   = origen.ZPESO_0,
               ZPALETS_0 = origen.ZPALETS_0,
               UPDDAT_0  = GETDATE()
WHEN NOT MATCHED THEN
    INSERT (SDHNUM_0, ZMATTRA_0, ZMATREM_0, ZPESO_0, ZPALETS_0, ZDECAEST_0, CREDAT_0, UPDDAT_0)
    VALUES (origen.SDHNUM_0, origen.ZMATTRA_0, origen.ZMATREM_0, origen.ZPESO_0, origen.ZPALETS_0,
            0, GETDATE(), GETDATE());
"""
    peso = matriculas.get("peso_kg")
    palets = matriculas.get("num_pallets")
    try:
        peso_val = float(peso) if peso is not None and str(peso).strip() != "" else None
    except (TypeError, ValueError):
        peso_val = None
    try:
        palets_val = int(float(palets)) if palets is not None and str(palets).strip() != "" else None
    except (TypeError, ValueError):
        palets_val = None

    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute(
            sql,
            numalbaran,
            matriculas.get("matricula_tractor"),
            matriculas.get("matricula_remolque"),
            peso_val,
            palets_val,
        )

        # Relacion transportista -> matriculas (carga automatica futura)
        codigo = (matriculas.get("transportista_codigo") or "").strip()
        if not codigo:
            cursor.execute(
                "SELECT BPTNUM_0 FROM LIVE.SDELIVERY WHERE SDHNUM_0 = ?",
                numalbaran,
            )
            fila = cursor.fetchone()
            codigo = str(fila[0]).strip() if fila and fila[0] else ""
        if codigo and matriculas.get("matricula_tractor"):
            sql_mat = """
MERGE LIVE.ZDECA_MATRICULA AS destino
USING (SELECT ? AS BPTNUM_0, ? AS ZMATTRA_0, ? AS ZMATREM_0) AS origen
   ON destino.BPTNUM_0 = origen.BPTNUM_0
WHEN MATCHED THEN
    UPDATE SET ZMATTRA_0 = origen.ZMATTRA_0,
               ZMATREM_0 = origen.ZMATREM_0,
               UPDDAT_0  = GETDATE()
WHEN NOT MATCHED THEN
    INSERT (BPTNUM_0, ZMATTRA_0, ZMATREM_0, CREDAT_0, UPDDAT_0)
    VALUES (origen.BPTNUM_0, origen.ZMATTRA_0, origen.ZMATREM_0, GETDATE(), GETDATE());
"""
            cursor.execute(
                sql_mat,
                codigo,
                matriculas.get("matricula_tractor"),
                matriculas.get("matricula_remolque"),
            )
        conexion.commit()
    logger.info("Datos editables guardados para el albaran %s", numalbaran)


def guardar_deca_generado(numalbaran: str, url: str) -> None:
    """Guarda la URL del DeCA y marca el albaran como generado (ZDECAEST_0 = 1)."""
    sql = """
MERGE LIVE.ZDECA AS destino
USING (SELECT ? AS SDHNUM_0, ? AS ZDECAURL_0) AS origen
   ON destino.SDHNUM_0 = origen.SDHNUM_0
WHEN MATCHED THEN
    UPDATE SET ZDECAURL_0 = origen.ZDECAURL_0,
               ZDECAEST_0 = 1,
               UPDDAT_0   = GETDATE()
WHEN NOT MATCHED THEN
    INSERT (SDHNUM_0, ZDECAURL_0, ZDECAEST_0, CREDAT_0, UPDDAT_0)
    VALUES (origen.SDHNUM_0, origen.ZDECAURL_0, 1, GETDATE(), GETDATE());
"""
    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute(sql, numalbaran, url)
        conexion.commit()
    logger.info("Albaran %s marcado como DeCA generado: %s", numalbaran, url)
