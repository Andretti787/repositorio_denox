# /project_root/models.py
from sqlalchemy import Column, Integer, String, Float, ForeignKey, Table, Date, DateTime, ForeignKeyConstraint
from sqlalchemy.orm import relationship
from database import Base

# Nueva Tabla/Modelo de Objeto de Asociación para datos de producción específicos de Máquina-Artículo
# Esta tabla reemplaza la tabla de asociación simple y almacena datos adicionales
class MaquinaArticuloProduccion(Base):
    __tablename__ = "maquina_articulo_produccion"

    maquina_id = Column(Integer, ForeignKey('maquinas_inyectoras.id', ondelete='CASCADE'), primary_key=True)
    articulo_id = Column(String(20), ForeignKey('articulos.id', ondelete='CASCADE'), primary_key=True) # Cambiado a String(20)
    tasa_produccion_hora = Column(Float, nullable=False) # Tasa de producción para esta combinación máquina/artículo
    tiempo_cambio_molde_horas = Column(Float, nullable=True) # Tiempo de cambio de molde opcional para esta combinación
    molde_requerido = Column(String(50), nullable=True) # Identificador de molde opcional para esta combinación

    # Define relaciones de vuelta a MaquinaInyectora y Articulo
    maquina = relationship("MaquinaInyectora", back_populates="articulo_produccion_links")
    articulo = relationship("Articulo", back_populates="maquina_produccion_links")

    def __repr__(self):
        return f"<MaquinaArticuloProduccion(maquina_id={self.maquina_id}, articulo_id={self.articulo_id}, tasa_produccion={self.tasa_produccion_hora})>"

# Modelo existente para Máquinas Inyectoras
class MaquinaInyectora(Base):
    __tablename__ = "maquinas_inyectoras"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    nombre = Column(String(100), unique=True, index=True, nullable=False)
    costo_hora = Column(Float, nullable=False)
    tiempo_cambio_molde_horas = Column(Float, nullable=False) # Tiempo en horas
    planta = Column(String(100), nullable=True) # Añadida columna para la planta

    # Relación con el objeto de asociación
    # 'articulo_produccion_links' será una lista de objetos MaquinaArticuloProduccion que enlazan esta máquina con artículos
    articulo_produccion_links = relationship(
        "MaquinaArticuloProduccion",
        back_populates="maquina",
        cascade="all, delete-orphan" # Eliminar enlaces de producción si se elimina la máquina
    )
    def __repr__(self):
        return f"<MaquinaInyectora(id={self.id}, nombre='{self.nombre}', costo_hora={self.costo_hora})>"

class Articulo(Base):
    __tablename__ = "articulos"

    id = Column(String(20), primary_key=True, index=True) # Cambiado a String(20) y eliminado autoincrement
    nombre = Column(String(100), unique=True, index=True, nullable=False)
    # Eliminada tasa_produccion_hora y molde_requerido, ahora están en MaquinaArticuloProduccion

    # Relación con el objeto de asociación
    # 'maquina_produccion_links' será una lista de objetos MaquinaArticuloProduccion que enlazan este artículo con máquinas
    maquina_produccion_links = relationship(
        "MaquinaArticuloProduccion",
        back_populates="articulo",
        cascade="all, delete-orphan" # Eliminar enlaces de producción si se elimina el artículo
    )
    
    def __repr__(self):
        return f"<Articulo(id={self.id}, nombre='{self.nombre}')>"

# --- Modelos adicionales a considerar para fases posteriores ---
# class Orden(Base):
#     __tablename__ = "ordenes"
#     id = Column(Integer, primary_key=True, index=True)
#     articulo_id = Column(Integer, ForeignKey('articulos.id'))
#     cantidad = Column(Integer)
#     fecha_entrega_deseada = Column(DateTime) # O Date
#     articulo = relationship("Articulo")
#     # ... otros campos como prioridad, cliente_id, etc.

# class PlanificacionResultado(Base):
#     __tablename__ = "planificacion_resultados" # Resultados de una ejecución del algoritmo
#     id = Column(Integer, primary_key=True, index=True)
#     maquina_id = Column(Integer, ForeignKey('maquinas_inyectoras.id'))
#     articulo_id = Column(Integer, ForeignKey('articulos.id'))
#     orden_id = Column(Integer, ForeignKey('ordenes.id')) # Opcional, si se planifica por orden
#     cantidad_producida = Column(Integer)
#     tiempo_inicio = Column(DateTime)
#     tiempo_fin = Column(DateTime)
#     costo_operacion = Column(Float)
#
#     # Relaciones
#     maquina = relationship("MaquinaInyectora") # Enlace a la máquina usada
#     articulo = relationship("Articulo") # Enlace al artículo producido
#     orden = relationship("Orden") # Enlace a la orden que motivó esta producción (opcional)
#
#     def __repr__(self):
#         return f"<PlanificacionResultado(id={self.id}, maquina_id={self.maquina_id}, articulo_id={self.articulo_id}, cantidad={self.cantidad_producida})>"

# --- Modelos adicionales actualizados para fases posteriores ---
class Orden(Base):
    __tablename__ = "ordenes"
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    codigo_pedido = Column(String(20), nullable=False, index=True) # Campo ya no es único
    articulo_id = Column(String(20), ForeignKey('articulos.id'), nullable=False) # Cambiado a String(20)
    cantidad = Column(Integer, nullable=False)
    fecha_entrega_deseada = Column(Date, nullable=True) # O Date si no se necesita la hora
    # Podríamos añadir otros campos como prioridad, cliente_id, etc.

    articulo = relationship("Articulo")
    # Relación de vuelta a los resultados de planificación que cumplen esta orden
    planning_results = relationship("PlanificacionResultado", back_populates="orden") # Necesita back_populates en PlanificacionResultado

    def __repr__(self):
        return f"<Orden(id={self.id}, codigo_pedido='{self.codigo_pedido}', articulo_id='{self.articulo_id}', cantidad={self.cantidad})>"

# Modelo para almacenar los resultados de una ejecución del algoritmo de planificación
class PlanificacionResultado(Base):
    __tablename__ = "planificacion_resultados"
    id = Column(Integer, primary_key=True, index=True, autoincrement=True) # Added autoincrement
    maquina_id = Column(Integer, ForeignKey('maquinas_inyectoras.id'), nullable=False) # Enlace a la máquina usada
    articulo_id = Column(String(20), ForeignKey('articulos.id'), nullable=False) # Enlace al artículo producido (String(20) to match Articulo ID)
    orden_id = Column(Integer, ForeignKey('ordenes.id'), nullable=True) # Opcional, si se planifica por orden
    cantidad_producida = Column(Integer, nullable=False)
    tiempo_inicio = Column(DateTime, nullable=False)
    tiempo_fin = Column(DateTime, nullable=False)
    costo_operacion = Column(Float, nullable=False) # Costo calculado para este segmento
    tiempo_cambio_molde_aplicado_horas = Column(Float, nullable=True, default=0.0) # Nuevo campo

    # Relaciones
    maquina = relationship("MaquinaInyectora")
    articulo = relationship("Articulo")
    orden = relationship("Orden", back_populates="planning_results") # Added back_populates

    def __repr__(self):
        return f"<PlanificacionResultado(id={self.id}, maquina_id={self.maquina_id}, articulo_id={self.articulo_id}, cantidad={self.cantidad_producida})>"

# El modelo PlanificacionResultado necesita ser revisado para enlazar correctamente
# a la configuración de producción específica (MaquinaArticuloProduccion) si es necesario,
# o simplemente almacenar los valores usados (tasa, tiempo cambio) directamente.
# La versión anterior comentada en el diff ya usaba FKs directas a maquina y articulo,
# lo cual es más simple y probablemente suficiente a menos que necesites el enlace directo
# al *registro específico* en maquina_articulo_produccion.
