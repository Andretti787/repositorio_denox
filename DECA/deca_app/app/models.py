"""Esquemas Pydantic de entrada y salida de la API REST."""
from typing import Optional

from pydantic import BaseModel, EmailStr, Field, field_validator

from .validators import validar_matricula


class MatriculasUpdate(BaseModel):
    """Campos editables del DeCA: matriculas, peso y palets."""

    matricula_tractor: str
    matricula_remolque: Optional[str] = None
    peso_kg: Optional[float] = None
    num_pallets: Optional[int] = None

    @field_validator("matricula_tractor")
    @classmethod
    def validar_tractor(cls, valor: str) -> str:
        return validar_matricula(valor, obligatoria=True)

    @field_validator("matricula_remolque")
    @classmethod
    def validar_remolque(cls, valor: Optional[str]) -> Optional[str]:
        return validar_matricula(valor, obligatoria=False)


class AlbaranOut(BaseModel):
    """Albaran tal y como lo consume el frontend."""

    numalbaran: str
    fecha_transporte: Optional[str] = None
    cargador_nombre: Optional[str] = None
    cargador_nif: Optional[str] = None
    cargador_domicilio: Optional[str] = None
    transportista_nombre: Optional[str] = None
    transportista_nif: Optional[str] = None
    transportista_codigo: Optional[str] = None
    origen: Optional[str] = None
    destino_ciudad: Optional[str] = None
    destino_cp: Optional[str] = None
    destino_pais: Optional[str] = None
    naturaleza_mercancia: Optional[str] = None
    peso_kg: Optional[float] = None
    num_pallets: Optional[int] = None
    autorizacion_especial: Optional[str] = None
    observaciones: Optional[str] = None
    matricula_tractor: Optional[str] = None
    matricula_remolque: Optional[str] = None
    deca_url: Optional[str] = None
    deca_estado: int = 0


class DecaMultiRequest(BaseModel):
    """Peticion de generacion de un DeCA consolidado para varios albaranes."""

    numalbaranes: list[str] = Field(..., min_length=1)


class DecaResponse(BaseModel):
    """Respuesta de la generacion y subida del DeCA."""

    numalbaran: str
    url: str
    estado: int
    mensaje: str


class EmailRequest(BaseModel):
    """Peticion de envio del DeCA por correo electronico."""

    email: EmailStr
