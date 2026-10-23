"""Normalizacion de matriculas.

No se valida el formato: se admiten matriculas espanolas y extranjeras
(libre texto). Solo se normaliza (mayusculas, sin espacios/guiones) y se
exige que la del tractor no este vacia.
"""


def normalizar_matricula(valor: str | None) -> str | None:
    """Recorta los bordes y convierte a mayusculas (sin alterar el resto).

    Se conservan espacios y guiones internos para admitir matriculas
    extranjeras (p. ej. "M-1234-AB"). Devuelve None cuando el valor esta
    vacio, para guardar NULL en la BD.
    """
    if valor is None:
        return None
    limpia = valor.strip().upper()
    return limpia or None


def validar_matricula(valor: str | None, obligatoria: bool = False) -> str | None:
    """Normaliza una matricula sin validar su formato (admite extranjeras).

    Args:
        valor: matricula tal y como la introduce el usuario.
        obligatoria: si es True, el valor vacio provoca error.

    Raises:
        ValueError: solo si la matricula es obligatoria y esta vacia.
    """
    limpia = normalizar_matricula(valor)
    if limpia is None and obligatoria:
        raise ValueError("La matricula del vehiculo tractor es obligatoria")
    return limpia
