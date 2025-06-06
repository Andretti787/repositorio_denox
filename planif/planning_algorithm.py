# c:\Trabajo\python CRUD\planif\planning_algorithm.py

from sqlalchemy.orm import Session
from models import Orden, MaquinaInyectora, MaquinaArticuloProduccion, PlanificacionResultado, Articulo
from typing import List
from datetime import datetime, timedelta # Necesario para simular el tiempo

def run_planning(db: Session, ordenes: List[Orden], maquinas: List[MaquinaInyectora]):
    """
    Ejecuta el algoritmo de planificación de producción.

    Args:
        db: Sesión de SQLAlchemy.
        ordenes: Lista de objetos Orden a planificar.
        maquinas: Lista de objetos MaquinaInyectora disponibles.

    Returns:
        Una lista de objetos PlanificacionResultado generados por el algoritmo.
    """
    print(f"Iniciando planificación para {len(ordenes)} órdenes en {len(maquinas)} máquinas.")

    # 1. Obtener las configuraciones de producción relevantes
    # Necesitamos las configuraciones para los artículos de las órdenes seleccionadas
    # que pueden ser producidos por las máquinas seleccionadas.
    ordenes_articulo_ids = [orden.articulo_id for orden in ordenes]
    maquinas_ids = [maquina.id for maquina in maquinas]

    # Consultar configuraciones de producción relevantes
    produccion_configs = db.query(MaquinaArticuloProduccion).filter(
        MaquinaArticuloProduccion.articulo_id.in_(ordenes_articulo_ids),
        MaquinaArticuloProduccion.maquina_id.in_(maquinas_ids)
    ).all()

    # Organizar las configuraciones por (maquina_id, articulo_id) para fácil acceso
    config_dict = {(config.maquina_id, config.articulo_id): config for config in produccion_configs}

    # 2. Preparar los datos para el algoritmo
    # Crear una lista de "tareas" potenciales (orden + máquina compatible)
    tareas_potenciales = []
    for orden in ordenes:
        for maquina in maquinas:
            # Verificar si esta máquina puede producir este artículo según las configuraciones
            config = config_dict.get((maquina.id, orden.articulo_id))
            if config:
                # Calcular tiempo de producción para esta orden en esta máquina
                tiempo_produccion_horas = orden.cantidad / config.tasa_produccion_hora

                # Usar tiempo de cambio de molde específico si existe, si no, usar el de la máquina
                tiempo_cambio_molde_horas = config.tiempo_cambio_molde_horas if config.tiempo_cambio_molde_horas is not None else maquina.tiempo_cambio_molde_horas

                # Calcular costo de producción para esta orden en esta máquina (solo tiempo de producción por ahora)
                # El costo total incluiría cambios de molde, que dependen de la secuencia
                costo_produccion = tiempo_produccion_horas * maquina.costo_hora

                tareas_potenciales.append({
                    'orden': orden,
                    'maquina': maquina,
                    'config': config,
                    'tiempo_produccion_horas': tiempo_produccion_horas,
                    'tiempo_cambio_molde_horas': tiempo_cambio_molde_horas,
                    'costo_produccion_base': costo_produccion, # Costo sin incluir cambios de molde
                    'fecha_entrega_deseada': orden.fecha_entrega_deseada # Para considerar tardanza
                })

    # 3. Implementar un algoritmo de asignación y secuenciación (Greedy Simplificado)
    resultados_planificacion = [] # Lista para almacenar los objetos PlanificacionResultado
    
    # Mantener un registro del tiempo de finalización y el último artículo producido en cada máquina
    estado_maquina = {
        maquina.id: {'tiempo_fin': datetime.now(), 'ultimo_articulo_id': None}
        for maquina in maquinas
    } # Iniciar con el tiempo actual
    
    # Ordenar las órdenes (por ejemplo, por fecha de entrega deseada, si existe)
    # Para este ejemplo simple, las procesaremos en el orden en que vienen.
    # Se podría añadir una lógica de priorización aquí.
    
    for tarea_potencial in sorted(tareas_potenciales, key=lambda t: (t['fecha_entrega_deseada'] is None, t['fecha_entrega_deseada'])): # Priorizar las que tienen fecha
        orden_actual = tarea_potencial['orden']
        maquina_actual = tarea_potencial['maquina']
        config_actual = tarea_potencial['config']
        
        # Verificar si la orden ya ha sido completamente planificada (si se divide entre máquinas)
        # Para este ejemplo simple, asumimos que una orden se asigna a una sola máquina.
        # Si una orden ya fue asignada, podríamos saltarla o manejar cantidades restantes.
        # Por ahora, si la orden ya está en resultados_planificacion, la saltamos.
        if any(res.orden_id == orden_actual.id for res in resultados_planificacion):
            continue

        tiempo_inicio_setup = estado_maquina[maquina_actual.id]['tiempo_fin'] # Inicio potencial del setup o producción
        ultimo_articulo_en_maquina = estado_maquina[maquina_actual.id]['ultimo_articulo_id']
        
        # DEBUG: Imprimir valores antes de la condición de cambio de molde
        print(f"DEBUG ALGORITHM: Máquina ID: {maquina_actual.id} ({maquina_actual.nombre}), "
              f"Procesando Orden ID: {orden_actual.id} (Artículo ID: {orden_actual.articulo_id}), "
              f"Último Artículo en Máquina: {ultimo_articulo_en_maquina}, "
              f"Tiempo de cambio de molde para esta tarea (de tarea_potencial): {tarea_potencial['tiempo_cambio_molde_horas']}")

        tiempo_cambio_aplicado_horas = 0.0 # Asegurar que sea float
        if ultimo_articulo_en_maquina is None or ultimo_articulo_en_maquina != orden_actual.articulo_id:
            # Aplicar tiempo de cambio de molde si es la primera tarea en la máquina
            # o si el artículo es diferente al anterior.
            # Asegurarse de que el tiempo de cambio no sea None antes de convertir a float
            valor_tiempo_cambio_tarea = tarea_potencial['tiempo_cambio_molde_horas']
            if valor_tiempo_cambio_tarea is not None:
                tiempo_cambio_aplicado_horas = float(valor_tiempo_cambio_tarea)
                if tiempo_cambio_aplicado_horas > 0: # Solo imprimir si realmente se aplica un tiempo > 0
                    print(f"INFO ALGORITHM: Aplicando {tiempo_cambio_aplicado_horas}h de cambio de molde en Máquina {maquina_actual.nombre} para Artículo {orden_actual.articulo_id}")
            else: # Si es None, podría significar 0 o que no se definió. Asumimos 0 para el cálculo.
                tiempo_cambio_aplicado_horas = 0.0
                print(f"WARN ALGORITHM: Tiempo de cambio de molde es None para tarea potencial (Máquina {maquina_actual.id}, Artículo {orden_actual.articulo_id}). Asumiendo 0h de cambio aplicado.")

        tiempo_inicio_produccion = tiempo_inicio_setup + timedelta(hours=tiempo_cambio_aplicado_horas)
        
        tiempo_produccion_td = timedelta(hours=tarea_potencial['tiempo_produccion_horas'])
        tiempo_fin_produccion = tiempo_inicio_produccion + tiempo_produccion_td
        
        # El costo debe incluir el cambio de molde aplicado
        costo_total_tarea = (tarea_potencial['tiempo_produccion_horas'] + tiempo_cambio_aplicado_horas) * maquina_actual.costo_hora
        
        # Crear el objeto PlanificacionResultado
        resultado = PlanificacionResultado(
            maquina_id=maquina_actual.id,
            articulo_id=orden_actual.articulo_id,
            orden_id=orden_actual.id,
            cantidad_producida=orden_actual.cantidad, # Asumimos que se produce toda la cantidad de la orden
            tiempo_inicio=tiempo_inicio_produccion,    # Inicio de la producción
            tiempo_fin=tiempo_fin_produccion,        # Fin de la producción
            costo_operacion=costo_total_tarea,
            tiempo_cambio_molde_aplicado_horas=tiempo_cambio_aplicado_horas # Guardar el tiempo de cambio
        )
        resultados_planificacion.append(resultado)
        
        # Actualizar el estado de la máquina
        estado_maquina[maquina_actual.id]['tiempo_fin'] = tiempo_fin_produccion # El fin de la máquina es el fin de la producción
        estado_maquina[maquina_actual.id]['ultimo_articulo_id'] = orden_actual.articulo_id
        
        print(f"INFO ALGORITHM: Asignada Orden ID {orden_actual.id} ({orden_actual.articulo.nombre}) a Máquina {maquina_actual.nombre}. "
              f"Setup: {tiempo_cambio_aplicado_horas}h, Prod de {tiempo_inicio_produccion} a {tiempo_fin_produccion}")

    # 4. Guardar los resultados en la base de datos
    if resultados_planificacion:
        try:
            db.add_all(resultados_planificacion)
            db.commit()
            print(f"INFO ALGORITHM: Guardados {len(resultados_planificacion)} resultados de planificación en la BD.")
        except Exception as e:
            db.rollback()
            print(f"ERROR ALGORITHM: Error al guardar resultados de planificación: {e}")
            # Podrías querer propagar este error o manejarlo de otra forma
            return [] # Devolver lista vacía si hay error al guardar

    if not resultados_planificacion and tareas_potenciales:
        print("INFO ALGORITHM: No se generaron resultados de planificación, aunque había tareas potenciales.")
    elif not tareas_potenciales:
        print("INFO ALGORITHM: No hay tareas potenciales para planificar (verificar compatibilidad máquina-artículo).")
        
    print("Planificación completada.")
    return resultados_planificacion # Devolver los resultados generados