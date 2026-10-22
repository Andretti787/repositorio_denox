"""Capa de acceso a datos: SQL Server (Sage X3) mediante pyodbc.

Mapeo de campos (esquema real comprobado en la base de datos x3):
  - Albaran:            LIVE.SDELIVERY
                        (SDHNUM_0, SHIDAT_0, GROWEI_0, PACNBR_0, STOFCY_0, BPTNUM_0,
                         BPDADDLIG_0/1/2, BPDCTY_0, BPDPOSCOD_0, BPDCRYNAM_0/BPDCRY_0)
  - Cliente:            LIVE.BPCUSTOMER (solo para busqueda/filtro)
  - Cargador/empresa:   LIVE.FACILITY (FCYNAM_0, CRN_0) + LIVE.BPADDRESS (domicilio)
  - Transportista:      LIVE.BPARTNER (BPRNAM_0, CRN_0) + LIVE.BPCARRIER.BPTNAM_0
  - Observaciones:      LIVE.ZDECA.ZOBSERV_0 (editables por albaran en la pantalla)
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

# Naturaleza de la mercancia en funcion del NIF del cargador contractual.
# Ampliable: anada aqui los NIF de sus cargadores con el texto que deba figurar en el DeCA.
NATURALEZAS_POR_CARGADOR = {
    "A50040112": "MENAJE DE PLÁSTICO",
    "B85851368": "Plásticos inyectados",
}


def normalizar_nif(valor) -> str:
    """Normaliza un NIF: sin espacios, guiones ni puntos y en mayusculas."""
    if valor is None:
        return ""
    return re.sub(r"[\s\-\.]", "", str(valor)).upper()


def naturaleza_por_cargador(cargador_nif) -> str:
    """Devuelve la naturaleza de la mercancia segun el NIF del cargador."""
    return NATURALEZAS_POR_CARGADOR.get(normalizar_nif(cargador_nif), NATURALEZA_FIJA)


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

# Expresion SQL del domicilio del cargador (direccion del sitio de carga)
_SQL_DOMICILIO_CARGADOR = (
    "NULLIF(LTRIM(RTRIM(CONCAT("
    "NULLIF(LTRIM(RTRIM(ADR.BPAADDLIG_0)), ''), N' ', "
    "NULLIF(LTRIM(RTRIM(ADR.BPAADDLIG_1)), ''), N' ', "
    "NULLIF(LTRIM(RTRIM(ADR.POSCOD_0)), ''), N' ', "
    "NULLIF(LTRIM(RTRIM(ADR.CTY_0)), ''), N' ', "
    "NULLIF(LTRIM(RTRIM(ADR.CRYNAM_0)), '')"
    "))), '')"
)

# Expresion SQL del transportista efectivo: valor editado en ZDECA si existe;
# si no, el nombre de BPARTNER/BPCARRIER y, en ultimo termino, el codigo.
_SQL_TRANSPORTISTA = (
    "COALESCE("
    "NULLIF(LTRIM(RTRIM(Z.ZTRANSP_0)), ''), "
    "NULLIF(LTRIM(RTRIM(TPT.BPRNAM_0)), ''), "
    "NULLIF(LTRIM(RTRIM(CPT.BPTNAM_0)), ''), "
    "DEL.BPTNUM_0)"
)

# Expresion SQL del NIF del transportista efectivo (editable en ZDECA)
_SQL_TRANSPORTISTA_NIF = (
    "COALESCE(NULLIF(LTRIM(RTRIM(Z.ZTRANSNIF_0)), ''), TPT.CRN_0)"
)
_SQL_BASE = """
SELECT
    DEL.SDHNUM_0                       AS numalbaran,
    CONVERT(varchar, DEL.SHIDAT_0, 23) AS fecha_transporte,
    COALESCE(NULLIF(LTRIM(RTRIM(FCY.FCYNAM_0)), ''), DEL.STOFCY_0) AS cargador_nombre,
    NULLIF(LTRIM(RTRIM(CAST(FCY.CRN_0 AS NVARCHAR(40)))), '') AS cargador_nif,
    {DOMICILIO_EXPR}                   AS cargador_domicilio,
    {TRANSPORTISTA_EXPR}              AS transportista_nombre,
    NULLIF(LTRIM(RTRIM(Z.ZTRANSP_0)), '') AS transportista_manual,
    DEL.BPTNUM_0                       AS transportista_codigo,
    {TRANSPORTISTA_NIF_EXPR}           AS transportista_nif,
    NULLIF(LTRIM(RTRIM(Z.ZTRANSNIF_0)), '') AS transportista_nif_manual,
    {ORIGEN_EXPR} AS origen,
    DEL.BPDCTY_0                       AS destino_ciudad,
    DEL.BPDPOSCOD_0                    AS destino_cp,
    COALESCE(NULLIF(LTRIM(RTRIM(DEL.BPDCRYNAM_0)), ''), DEL.BPDCRY_0) AS destino_pais,
    NULLIF(LTRIM(RTRIM(Z.ZDESTINO_0)), '') AS destino_manual,
    CAST('' AS NVARCHAR(240))          AS naturaleza_mercancia,
    COALESCE(Z.ZPESO_0, DEL.GROWEI_0)  AS peso_kg,
    CAST(COALESCE(Z.ZPALETS_0, DEL.PACNBR_0) AS FLOAT) AS num_pallets,
    CAST('' AS NVARCHAR(60))           AS autorizacion_especial,
    NULLIF(LTRIM(RTRIM(Z.ZOBSERV_0)), '') AS observaciones_manual,
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
"""
_SQL_BASE = _SQL_BASE.replace("{ORIGEN_EXPR}", _SQL_ORIGEN)
_SQL_BASE = _SQL_BASE.replace("{DOMICILIO_EXPR}", _SQL_DOMICILIO_CARGADOR)
_SQL_BASE = _SQL_BASE.replace("{TRANSPORTISTA_EXPR}", _SQL_TRANSPORTISTA)
_SQL_BASE = _SQL_BASE.replace("{TRANSPORTISTA_NIF_EXPR}", _SQL_TRANSPORTISTA_NIF)




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


def componer_destino(datos: dict) -> str:
    """Texto del lugar de destino.

    Prioridad:
      1. Valor manual editado por el usuario (LIVE.ZDECA.ZDESTINO_0).
      2. Destino del albaran en Sage: CP + ciudad + (pais).
    """
    manual = _limpiar_texto(datos.get("destino_manual")) or _limpiar_texto(datos.get("destino"))
    if manual:
        return manual
    cp = _limpiar_texto(datos.get("destino_cp"))
    ciudad = _limpiar_texto(datos.get("destino_ciudad"))
    pais = _limpiar_texto(datos.get("destino_pais"))
    partes = []
    if cp:
        partes.append(cp)
    if ciudad:
        partes.append(ciudad)
    if pais:
        partes.append(f"({pais})")
    return " ".join(partes).strip()


def _normalizar_texto_libre(valor, maximo: int) -> str | None:
    """Normaliza un texto editable: espacios simples, recorte y limite de longitud.

    Devuelve None cuando el campo viene vacio (se guardara NULL en la base).
    """
    texto = _limpiar_texto(valor)
    return texto[:maximo] if texto else None


def _normalizar_destino(destino, destino_auto) -> str | None:
    """Normaliza el destino editable antes de guardarlo en ZDESTINO_0.

    Devuelve None cuando no hay modificacion manual: bien porque el campo esta
    vacio (se usara el destino de SDELIVERY), bien porque el texto coincide con
    el destino calculado que ya muestra la pantalla.
    """
    texto = _limpiar_texto(destino)
    if not texto:
        return None
    automatico = _limpiar_texto(destino_auto)
    if automatico and texto.upper() == automatico.upper():
        return None
    return texto[:160]


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
    # Naturaleza en funcion del NIF del cargador contractual
    datos["naturaleza_mercancia"] = naturaleza_por_cargador(cargador_nif)
    # Transportista efectivo: valor editado en ZDECA si existe
    datos["transportista_manual"] = _limpiar_texto(datos.get("transportista_manual"))
    datos["transportista_nombre"] = (
        datos["transportista_manual"]
        or _limpiar_texto(datos.get("transportista_nombre"))
        or _limpiar_texto(datos.get("transportista_codigo"))
    )
    datos["transportista_nif_manual"] = _limpiar_texto(datos.get("transportista_nif_manual"))
    datos["transportista_nif"] = (
        datos["transportista_nif_manual"] or _limpiar_texto(datos.get("transportista_nif"))
    )
    # Destino: manual (ZDESTINO_0) si existe; si no, CP + ciudad + (pais)
    datos["destino_manual"] = _limpiar_texto(datos.get("destino_manual"))
    datos["destino"] = componer_destino(datos)
    # Observaciones: texto editable guardado en ZOBSERV_0 (ya no viene de BPDLVCUST)
    datos["observaciones_manual"] = _limpiar_texto(datos.get("observaciones_manual"))
    datos["observaciones"] = datos["observaciones_manual"]
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
        "transportista_nombre": _SQL_TRANSPORTISTA,
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
            "(" + _SQL_TRANSPORTISTA + " LIKE ? "
            "OR DEL.BPTNUM_0 LIKE ? OR " + _SQL_TRANSPORTISTA_NIF + " LIKE ?)"
        )
        parametros.extend([comodin_t, comodin_t, comodin_t])

    filtro = (filtro or "").strip()
    if filtro:
        comodin = f"%{filtro}%"
        condiciones.append(
            "(DEL.SDHNUM_0 LIKE ? OR CLI.BPCNAM_0 LIKE ? OR DEL.BPDCTY_0 LIKE ? OR "
            + _SQL_ORIGEN + " LIKE ? OR " + _SQL_TRANSPORTISTA + " LIKE ?)"
        )
        parametros.extend([comodin, comodin, comodin, comodin, comodin])

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


def listar_origenes_detalle() -> list[dict]:
    """Origenes (CP + ciudad) junto con los datos del cargador contractual.

    Para cada origen se toma el sitio de carga con mas albaranes, de modo que el
    formulario de DeCA sin albaran pueda arrastrar nombre, NIF y domicilio.
    """
    sql = f"""
WITH por_sitio AS (
    SELECT
        {_SQL_ORIGEN} AS origen,
        DEL.STOFCY_0  AS sitio,
        COUNT(*)      AS albaranes
    FROM LIVE.SDELIVERY DEL
    OUTER APPLY (
        SELECT TOP 1 A.POSCOD_0, A.CTY_0
        FROM LIVE.BPADDRESS A
        WHERE A.BPANUM_0 = DEL.STOFCY_0
          AND A.BPATYP_0 = 3
    ) ADR
    WHERE {_SQL_ORIGEN} IS NOT NULL
    GROUP BY {_SQL_ORIGEN}, DEL.STOFCY_0
),
elegido AS (
    SELECT origen, sitio, albaranes,
           ROW_NUMBER() OVER (PARTITION BY origen ORDER BY albaranes DESC, sitio) AS posicion
    FROM por_sitio
)
SELECT
    E.origen,
    E.sitio,
    E.albaranes,
    COALESCE(NULLIF(LTRIM(RTRIM(FCY.FCYNAM_0)), ''), E.sitio) AS cargador_nombre,
    NULLIF(LTRIM(RTRIM(CAST(FCY.CRN_0 AS NVARCHAR(40)))), '')  AS cargador_nif,
    {_SQL_DOMICILIO_CARGADOR}                                  AS cargador_domicilio
FROM elegido E
LEFT JOIN LIVE.FACILITY FCY ON FCY.FCY_0 = E.sitio
OUTER APPLY (
    SELECT TOP 1 A.BPAADDLIG_0, A.BPAADDLIG_1, A.POSCOD_0, A.CTY_0, A.CRYNAM_0
    FROM LIVE.BPADDRESS A
    WHERE A.BPANUM_0 = E.sitio
      AND A.BPATYP_0 = 3
) ADR
WHERE E.posicion = 1
ORDER BY E.albaranes DESC, E.origen
"""
    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute("SET LOCK_TIMEOUT 10000")
        cursor.execute(sql)
        columnas = [c[0] for c in cursor.description]
        filas = [
            {col: _serializar(valor) for col, valor in zip(columnas, fila)}
            for fila in cursor.fetchall()
        ]

    for fila in filas:
        fila["origen"] = _limpiar_texto(fila.get("origen"))
        fila["cargador_nombre"] = _limpiar_texto(fila.get("cargador_nombre"))
        fila["cargador_nif"] = _limpiar_texto(fila.get("cargador_nif"))
        fila["cargador_domicilio"] = _limpiar_texto(fila.get("cargador_domicilio"))
        # Naturaleza derivada del NIF del cargador (la misma regla que en el listado)
        fila["naturaleza_mercancia"] = naturaleza_por_cargador(fila["cargador_nif"])
    return filas


def obtener_cargador(origen: str = "", sitio: str = "") -> dict | None:
    """Datos del cargador contractual para un origen o un sitio de carga.

    Se usa en la generacion de un DeCA sin albaran: el usuario elige el origen y
    la aplicacion arrastra nombre, NIF, domicilio y naturaleza de la mercancia.
    """
    condiciones = []
    parametros: list = []
    if (sitio or "").strip():
        condiciones.append("FCY.FCY_0 = ?")
        parametros.append(sitio.strip())
    elif (origen or "").strip():
        condiciones.append(f"{_SQL_ORIGEN} = ?")
        parametros.append(origen.strip())
    else:
        return None

    sql = f"""
SELECT TOP 1
    {_SQL_ORIGEN} AS origen,
    FCY.FCY_0 AS sitio,
    COALESCE(NULLIF(LTRIM(RTRIM(FCY.FCYNAM_0)), ''), FCY.FCY_0) AS cargador_nombre,
    NULLIF(LTRIM(RTRIM(CAST(FCY.CRN_0 AS NVARCHAR(40)))), '') AS cargador_nif,
    {_SQL_DOMICILIO_CARGADOR} AS cargador_domicilio,
    ISNULL(USO.albaranes, 0) AS albaranes
FROM LIVE.FACILITY FCY
OUTER APPLY (
    SELECT TOP 1 A.BPAADDLIG_0, A.BPAADDLIG_1, A.POSCOD_0, A.CTY_0, A.CRYNAM_0
    FROM LIVE.BPADDRESS A
    WHERE A.BPANUM_0 = FCY.FCY_0
      AND A.BPATYP_0 = 3
) ADR
OUTER APPLY (
    -- Varios sitios pueden compartir direccion: se prefiere el que mas albaranes tiene
    SELECT COUNT(*) AS albaranes FROM LIVE.SDELIVERY D WHERE D.STOFCY_0 = FCY.FCY_0
) USO
WHERE {' AND '.join(condiciones)}
ORDER BY ISNULL(USO.albaranes, 0) DESC, FCY.FCY_0
"""
    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute("SET LOCK_TIMEOUT 10000")
        cursor.execute(sql, parametros)
        fila = cursor.fetchone()
        if fila is None:
            return None
        columnas = [c[0] for c in cursor.description]
        datos = {col: _serializar(valor) for col, valor in zip(columnas, fila)}

    for clave in ("origen", "sitio", "cargador_nombre", "cargador_nif", "cargador_domicilio"):
        datos[clave] = _limpiar_texto(datos.get(clave))
    datos["naturaleza_mercancia"] = naturaleza_por_cargador(datos["cargador_nif"])
    return datos


def generar_referencia_manual() -> str:
    """Referencia unica para un DeCA creado sin albaran (max. 20 caracteres)."""
    return "MAN-" + datetime.now().strftime("%Y%m%d-%H%M%S")


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
    """UPSERT en LIVE.ZDECA de los campos editables del DeCA.

    Editable: matriculas, peso, palets, destino, observaciones, transportista y su NIF.
    Si el albaran tiene transportista, tambien actualiza LIVE.ZDECA_MATRICULA
    para que futuros albaranes del mismo transportista hereden las matriculas.

    El destino se guarda en ZDESTINO_0. Si el texto recibido coincide con el
    destino calculado (CP + ciudad + pais de SDELIVERY) se guarda NULL, para no
    fijar una modificacion manual innecesaria.
    """
    sql = """
MERGE LIVE.ZDECA AS destino
USING (SELECT ? AS SDHNUM_0, ? AS ZMATTRA_0, ? AS ZMATREM_0,
              ? AS ZPESO_0, ? AS ZPALETS_0, ? AS ZDESTINO_0,
              ? AS ZOBSERV_0, ? AS ZTRANSP_0, ? AS ZTRANSNIF_0) AS origen
   ON destino.SDHNUM_0 = origen.SDHNUM_0
WHEN MATCHED THEN
    UPDATE SET ZMATTRA_0   = origen.ZMATTRA_0,
               ZMATREM_0   = origen.ZMATREM_0,
               ZPESO_0     = origen.ZPESO_0,
               ZPALETS_0   = origen.ZPALETS_0,
               ZDESTINO_0  = origen.ZDESTINO_0,
               ZOBSERV_0   = origen.ZOBSERV_0,
               ZTRANSP_0   = origen.ZTRANSP_0,
               ZTRANSNIF_0 = origen.ZTRANSNIF_0,
               UPDDAT_0    = GETDATE()
WHEN NOT MATCHED THEN
    INSERT (SDHNUM_0, ZMATTRA_0, ZMATREM_0, ZPESO_0, ZPALETS_0, ZDESTINO_0,
            ZOBSERV_0, ZTRANSP_0, ZTRANSNIF_0, ZDECAEST_0, CREDAT_0, UPDDAT_0)
    VALUES (origen.SDHNUM_0, origen.ZMATTRA_0, origen.ZMATREM_0, origen.ZPESO_0,
            origen.ZPALETS_0, origen.ZDESTINO_0, origen.ZOBSERV_0,
            origen.ZTRANSP_0, origen.ZTRANSNIF_0, 0, GETDATE(), GETDATE());
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

    # Destino editable: vacio -> NULL (se usa el de SDELIVERY).
    # Si coincide con el destino calculado (destino_auto) tampoco se fija override.
    destino_val = _normalizar_destino(matriculas.get("destino"), matriculas.get("destino_auto"))
    # Observaciones editables por albaran (texto libre)
    observaciones_val = _normalizar_texto_libre(matriculas.get("observaciones"), 320)
    # Transportista y NIF editables
    transportista_val = _normalizar_texto_libre(matriculas.get("transportista_nombre"), 80)
    transportista_nif_val = _normalizar_texto_libre(matriculas.get("transportista_nif"), 20)

    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute(
            sql,
            numalbaran,
            matriculas.get("matricula_tractor"),
            matriculas.get("matricula_remolque"),
            peso_val,
            palets_val,
            destino_val,
            observaciones_val,
            transportista_val,
            transportista_nif_val,
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


def guardar_deca_manual(referencia: str, datos: dict, url: str) -> None:
    """Registra en LIVE.ZDECA un DeCA generado sin albaran.

    Guarda origen, fecha del transporte y todos los campos editables, y deja el
    registro marcado como generado (ZDECAEST_0 = 1) con la URL publica del PDF.
    """
    sql = """
MERGE LIVE.ZDECA AS destino
USING (SELECT ? AS SDHNUM_0, ? AS ZORIGEN_0, ? AS ZFECHA_0, ? AS ZMATTRA_0,
              ? AS ZMATREM_0, ? AS ZDESTINO_0, ? AS ZOBSERV_0,
              ? AS ZPESO_0, ? AS ZPALETS_0,
              ? AS ZTRANSP_0, ? AS ZTRANSNIF_0, ? AS ZDECAURL_0) AS origen
   ON destino.SDHNUM_0 = origen.SDHNUM_0
WHEN MATCHED THEN
    UPDATE SET ZORIGEN_0   = origen.ZORIGEN_0,
               ZFECHA_0    = origen.ZFECHA_0,
               ZMATTRA_0   = origen.ZMATTRA_0,
               ZMATREM_0   = origen.ZMATREM_0,
               ZDESTINO_0  = origen.ZDESTINO_0,
               ZOBSERV_0   = origen.ZOBSERV_0,
               ZPESO_0     = origen.ZPESO_0,
               ZPALETS_0   = origen.ZPALETS_0,
               ZTRANSP_0   = origen.ZTRANSP_0,
               ZTRANSNIF_0 = origen.ZTRANSNIF_0,
               ZDECAURL_0  = origen.ZDECAURL_0,
               ZDECAEST_0  = 1,
               UPDDAT_0    = GETDATE()
WHEN NOT MATCHED THEN
    INSERT (SDHNUM_0, ZORIGEN_0, ZFECHA_0, ZMATTRA_0, ZMATREM_0, ZDESTINO_0,
            ZOBSERV_0, ZPESO_0, ZPALETS_0, ZTRANSP_0, ZTRANSNIF_0,
            ZDECAURL_0, ZDECAEST_0, CREDAT_0, UPDDAT_0)
    VALUES (origen.SDHNUM_0, origen.ZORIGEN_0, origen.ZFECHA_0, origen.ZMATTRA_0,
            origen.ZMATREM_0, origen.ZDESTINO_0, origen.ZOBSERV_0, origen.ZPESO_0,
            origen.ZPALETS_0, origen.ZTRANSP_0, origen.ZTRANSNIF_0,
            origen.ZDECAURL_0, 1, GETDATE(), GETDATE());
"""
    fecha = (datos.get("fecha_transporte") or "").strip() or None
    if fecha is not None:
        try:
            fecha = datetime.strptime(fecha[:10], "%Y-%m-%d").date().isoformat()
        except ValueError:
            fecha = None

    with get_connection() as conexion:
        cursor = conexion.cursor()
        cursor.execute(
            sql,
            referencia,
            _normalizar_texto_libre(datos.get("origen"), 120),
            fecha,
            datos.get("matricula_tractor"),
            datos.get("matricula_remolque"),
            _normalizar_texto_libre(datos.get("destino"), 320),
            _normalizar_texto_libre(datos.get("observaciones"), 320),
            datos.get("peso_kg"),
            datos.get("num_pallets"),
            _normalizar_texto_libre(datos.get("transportista_nombre"), 80),
            _normalizar_texto_libre(datos.get("transportista_nif"), 20),
            url,
        )
        conexion.commit()
    logger.info("DeCA sin albaran %s registrado: %s", referencia, url)
