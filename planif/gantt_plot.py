import matplotlib
matplotlib.use('Agg') # Usar backend no interactivo para evitar problemas en entornos sin GUI
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from datetime import datetime, timedelta
import os
import random # Para colores aleatorios

def generar_gantt_chart(resultados_planificacion, maquinas_dict, output_folder='static/plots'):
    print(f"DEBUG gantt_plot: Entrando a generar_gantt_chart con {len(resultados_planificacion) if resultados_planificacion else 0} resultados.")
    if not resultados_planificacion:
        print("DEBUG gantt_plot: No hay resultados de planificación para generar Gantt.")
        return None


    fig, ax = plt.subplots(figsize=(18, 10)) # Aumentar tamaño para mejor visualización

    maquina_nombres = {m.id: m.nombre for m_id, m in maquinas_dict.items()}
    
    # Crear un mapeo de ID de máquina a posición en el eje Y y colores
    unique_maquina_ids = sorted(list(set(res.maquina_id for res in resultados_planificacion)))
    maquina_y_pos = {maquina_id: i for i, maquina_id in enumerate(unique_maquina_ids)}
    
    # Generar colores aleatorios para cada orden (o tarea individual si no hay orden_id)
    tarea_colores = {}
    
    for i, resultado in enumerate(resultados_planificacion):
        # Tiempos de producción
        start_time_produccion = resultado.tiempo_inicio
        end_time_produccion = resultado.tiempo_fin
        
        # Tiempos de cambio de molde (si aplica)
        tiempo_cambio_horas = resultado.tiempo_cambio_molde_aplicado_horas or 0.0 # default a 0.0 si es None
        
        # El inicio del cambio de molde es el inicio de la producción menos la duración del cambio
        start_time_cambio_molde = start_time_produccion - timedelta(hours=tiempo_cambio_horas)
        # El fin del cambio de molde es el inicio de la producción

        maquina_id = resultado.maquina_id
        y_pos = maquina_y_pos.get(maquina_id)
        if y_pos is None: continue # Saltar si la máquina no está en la lista (no debería pasar)

        # Dibujar barra de cambio de molde (si aplica)
        if tiempo_cambio_horas > 0:
            ax.barh(y_pos, 
                    mdates.date2num(start_time_produccion) - mdates.date2num(start_time_cambio_molde), 
                    left=mdates.date2num(start_time_cambio_molde), 
                    height=0.6, 
                    color='black', # Color negro para cambio de molde
                    edgecolor='grey')
            # Añadir texto para cambio de molde (opcional, puede saturar el gráfico)
            ax.text(mdates.date2num(start_time_cambio_molde) + (mdates.date2num(start_time_produccion) - mdates.date2num(start_time_cambio_molde))/2, y_pos,
                     "CM", ha='center', va='center', color='white', fontsize=7, fontweight='bold')

        # Dibujar barra de producción
        color_key = resultado.orden_id if resultado.orden_id is not None else f"task_{resultado.id}"
        if color_key not in tarea_colores:
            # Generar un color aleatorio
            tarea_colores[color_key] = (random.random(), random.random(), random.random())
        color = tarea_colores[color_key]
        
        texto_orden = f"O-{resultado.orden.codigo_pedido}" if resultado.orden and resultado.orden.codigo_pedido else \
                      (f"Oid-{resultado.orden_id}" if resultado.orden_id else "Tarea")

        articulo_id_actual = resultado.articulo_id # Definir articulo_id_actual aquí

        ax.barh(y_pos, 
                mdates.date2num(end_time_produccion) - mdates.date2num(start_time_produccion), 
                left=mdates.date2num(start_time_produccion), height=0.6,
                label=f"{texto_orden}-A{articulo_id_actual}", color=color, edgecolor='black')
        
        # Añadir texto dentro de la barra
        ax.text(mdates.date2num(start_time_produccion) + (mdates.date2num(end_time_produccion) - mdates.date2num(start_time_produccion))/2, y_pos, 
                f"{texto_orden}\n{resultado.articulo.nombre[:15]}",
                ha='center', va='center', color='white' if sum(color) < 1.5 else 'black', fontsize=9, fontweight='bold') # Aumentado fontsize

    ax.set_yticks([maquina_y_pos[m_id] for m_id in unique_maquina_ids])
    ax.set_yticklabels([maquina_nombres[m_id] for m_id in unique_maquina_ids], fontsize=10) # Aumentado fontsize
    
    ax.xaxis_date()
    ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d %H:%M'))
    plt.xticks(fontsize=10) # Aumentado fontsize para las marcas del eje X
    fig.autofmt_xdate() # Rotar fechas para mejor visualización

    plt.xlabel("Tiempo", fontsize=12) # Aumentado fontsize
    plt.ylabel("Máquinas", fontsize=12) # Aumentado fontsize
    plt.title("Diagrama de Gantt de Planificación", fontsize=14) # Aumentado fontsize
    plt.grid(True, axis='x', linestyle=':')
    plt.tight_layout()

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    

    timestamp = datetime.now().strftime("%Y%m%d%H%M%S%f")
    plot_filename = f"gantt_chart_{timestamp}.png"
    # Asegurarse de que output_folder sea una ruta absoluta para os.makedirs si es necesario, o que sea relativa al CWD.
    # Para Flask, 'static/plots' es relativo al directorio de la aplicación.
    plot_path = os.path.abspath(os.path.join(output_folder, plot_filename)) # Usar ruta absoluta para el guardado
    print(f"DEBUG gantt_plot: Intentando guardar Gantt en: {plot_path}")
    
    try:
        plt.savefig(plot_path)
        print(f"DEBUG gantt_plot: Gantt guardado exitosamente en {plot_path}")
        # Devolver ruta relativa usando siempre barras inclinadas para compatibilidad con URL
        return f"plots/{plot_filename}"
    except Exception as e:
        print(f"ERROR gantt_plot: Error al guardar el diagrama de Gantt: {e}")
        return None
    finally:
        plt.close(fig) # Asegurar que la figura se cierre
