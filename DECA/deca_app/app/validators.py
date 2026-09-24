"""Validacion y normalizacion de matriculas espanolas.

Formatos admitidos:
  - Actual (desde septiembre de 2000): 4 digitos + 3 letras (1234BCD).
  - Remolques/semirremolques: R + 4 digitos + 3 letras (R1234BCD).
  - Antiguo provincial (hasta 2000): 1-2 letras + 4 digitos + 0-2 letras (M1234AB).
"""
import re

# Letras admitidas: cualquier letra A-Z. La serie oficial de la DGT excluye las
# vocales y las letras N y Q, pero se admite A-Z para no rechazar matriculas
# historicas/extranjeras ni ejemplos habituales (p. ej. 1234ABC).
RE_MATRICULA_ACTUAL = re.compile(r"^\d{4}[A-Z]{3}$")
RE_MATRICULA_REMOLQUE = re.compile(r"^R\d{4}[A-Z]{3}$")
RE_MATRICULA_ANTIGUA = re.compile(r"^[A-Z]{1,2}\d{4}[A-Z]{0,2}$")


def normalizar_matricula(valor: str | None) -> str | None:
    """Elimina espacios y guiones y convierte a mayusculas.

    Devuelve None cuando el valor esta vacio, para guardar NULL en la BD.
    """
    if valor is None:
        return None
    limpia = valor.strip().upper().replace(" ", "").replace("-", "")
    return limpia or None


def es_matricula_valida(valor: str) -> bool:
    """Comprueba si una matricula ya normalizada cumple algun formato valido."""
    return bool(
        RE_MATRICULA_ACTUAL.match(valor)
        or RE_MATRICULA_REMOLQUE.match(valor)
        or RE_MATRICULA_ANTIGUA.match(valor)
    )


def validar_matricula(valor: str | None, obligatoria: bool = False) -> str | None:
    """Normaliza y valida una matricula.

    Args:
        valor: matricula tal y como la introduce el usuario.
        obligatoria: si es True, el valor vacio provoca error.

    Raises:
        ValueError: si el formato no es valido (FastAPI lo devuelve como HTTP 422).
    """
    limpia = normalizar_matricula(valor)
    if limpia is None:
        if obligatoria:
            raise ValueError("La matricula del vehiculo tractor es obligatoria")
        return None
    if not es_matricula_valida(limpia):
        raise ValueError(f"Matricula '{valor}' con formato no valido (ejemplos: 1234BCD, R1234BCD, M1234AB)")
    return limpia