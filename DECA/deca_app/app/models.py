"""Esquemas Pydantic de entrada y salida de la API REST."""
from typing import Optional

from pydantic import BaseModel, EmailStr, Field, field_validator

from .validators import validar_matricula


class MatriculasUpdate(BaseModel):
    """Campos editables del DeCA: matriculas, peso, palets y destino."""

    matricula_tractor: str
    matricula_remolque: Optional[str] = None
    peso_kg: Optional[float] = None
    num_pallets: Optional[int] = None
    # Destino editado por el usuario. Vacio -> se usa el destino del albaran.
    destino: Optional[str] = Field(default=None, max_length=160)
    # Destino calculado (CP + ciudad + pais) que muestra la pantalla. Se usa para
    # no guardar como manual un texto que coincide con el automatico.
    destino_auto: Optional[str] = Field(default=None, max_length=160)
    # Observaciones editables del albaran. Se imprimen en el DeCA.
    observaciones: Optional[str] = Field(default=None, max_length=320)
    # Transportista efectivo y su NIF (editables)
    transportista_nombre: Optional[str] = Field(default=None, max_length=80)
    transportista_nif: Optional[str] = Field(default=None, max_length=20)

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
    # Valor guardado manualmente en LIVE.ZDECA (None = tomado de Sage)
    transportista_manual: Optional[str] = None
    transportista_nif_manual: Optional[str] = None
    transportista_codigo: Optional[str] = None
    origen: Optional[str] = None
    destino_ciudad: Optional[str] = None
    destino_cp: Optional[str] = None
    destino_pais: Optional[str] = None
    # Destino efectivo mostrado y usado en el PDF (manual si existe, si no CP+ciudad)
    destino: Optional[str] = None
    # Destino guardado manualmente en LIVE.ZDECA.ZDESTINO_0 (None = automatico)
    destino_manual: Optional[str] = None
    naturaleza_mercancia: Optional[str] = None
    peso_kg: Optional[float] = None
    num_pallets: Optional[int] = None
    autorizacion_especial: Optional[str] = None
    # Observaciones efectivas del DeCA (texto editable guardado en ZOBSERV_0)
    observaciones: Optional[str] = None
    # Valor guardado manualmente en LIVE.ZDECA.ZOBSERV_0
    observaciones_manual: Optional[str] = None
    matricula_tractor: Optional[str] = None
    matricula_remolque: Optional[str] = None
    deca_url: Optional[str] = None
    deca_estado: int = 0


class DecaMultiRequest(BaseModel):
    """Peticion de generacion de un DeCA consolidado para varios albaranes."""

    numalbaranes: list[str] = Field(..., min_length=1)


class OrigenDetalle(BaseModel):
    """Origen (CP + ciudad) con los datos del cargador contractual asociado."""

    origen: str
    sitio: Optional[str] = None
    albaranes: Optional[int] = None
    cargador_nombre: Optional[str] = None
    cargador_nif: Optional[str] = None
    cargador_domicilio: Optional[str] = None
    naturaleza_mercancia: Optional[str] = None


class DecaManualRequest(BaseModel):
    """DeCA creado sin albaran: se rellenan los campos editables a mano."""

    origen: str = Field(..., min_length=1, max_length=120)
    fecha_transporte: Optional[str] = Field(default=None, max_length=10)
    destino: Optional[str] = Field(default=None, max_length=160)
    transportista_nombre: Optional[str] = Field(default=None, max_length=80)
    transportista_nif: Optional[str] = Field(default=None, max_length=20)
    matricula_tractor: str
    matricula_remolque: Optional[str] = None
    peso_kg: Optional[float] = None
    num_pallets: Optional[int] = None
    observaciones: Optional[str] = Field(default=None, max_length=320)

    @field_validator("matricula_tractor")
    @classmethod
    def validar_tractor(cls, valor: str) -> str:
        return validar_matricula(valor, obligatoria=True)

    @field_validator("matricula_remolque")
    @classmethod
    def validar_remolque(cls, valor: Optional[str]) -> Optional[str]:
        return validar_matricula(valor, obligatoria=False)


class DecaResponse(BaseModel):
    """Respuesta de la generacion y subida del DeCA."""

    numalbaran: str
    url: str
    estado: int
    mensaje: str


class EmailRequest(BaseModel):
    """Peticion de envio del DeCA por correo electronico."""

    email: EmailStr
