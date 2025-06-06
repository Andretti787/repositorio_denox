from flask import Flask, render_template, request, redirect, url_for, flash
from database import get_db, init_db
from models import MaquinaInyectora, Articulo, MaquinaArticuloProduccion, Orden, PlanificacionResultado
from datetime import datetime # Necesario para manejar fechas/horas
from planning_algorithm import run_planning # Importar la función de planificación
import os # Importar os para manejo de rutas
from gantt_plot import generar_gantt_chart # Importar la función para el Gantt
from sqlalchemy.exc import IntegrityError

app = Flask(__name__)
app.secret_key = 'tu_clave_secreta_planificacion' # ¡Cambia esto!

# Inicializar la base de datos (crear tablas si no existen)
# Es mejor ejecutar create_tables.py una vez por separado,
# pero esto puede ser útil para asegurar que existan.
# init_db() # Comentado por ahora, asumiendo que create_tables.py se ejecuta manualmente.

@app.route('/')
def index():
    return redirect(url_for('listar_maquinas'))

@app.route('/maquinas')
def listar_maquinas():
    db = next(get_db()) # Obtener la sesión
    try:
        maquinas = db.query(MaquinaInyectora).order_by(MaquinaInyectora.nombre).all()
        return render_template('maquinas_lista.html', maquinas=maquinas) # Renderizar plantilla con sesión activa
    finally:
        db.close() # Asegurar que la sesión se cierre

@app.route('/maquinas/nueva', methods=['GET', 'POST'])
def agregar_maquina():
    if request.method == 'POST':
        nombre = request.form.get('nombre')
        costo_hora_str = request.form.get('costo_hora')
        tiempo_cambio_molde_horas_str = request.form.get('tiempo_cambio_molde_horas')
        planta = request.form.get('planta')

        if not nombre or not costo_hora_str or not tiempo_cambio_molde_horas_str:
            flash('Nombre, costo por hora y tiempo de cambio de molde son obligatorios.', 'danger')
            return render_template('maquina_form.html', maquina=request.form, action='nueva')

        try:
            costo_hora = float(costo_hora_str)
            tiempo_cambio_molde_horas = float(tiempo_cambio_molde_horas_str)
            if costo_hora < 0 or tiempo_cambio_molde_horas < 0:
                raise ValueError("Los valores numéricos no pueden ser negativos.")
        except ValueError as e:
            flash(f'Error en los valores numéricos: {e}', 'danger')
            return render_template('maquina_form.html', maquina=request.form, action='nueva')

        nueva_maquina = MaquinaInyectora(
            nombre=nombre,
            costo_hora=costo_hora,
            tiempo_cambio_molde_horas=tiempo_cambio_molde_horas,
            planta=planta if planta else None
        )
        db = next(get_db())
        try:
            db.add(nueva_maquina)
            db.commit()
            flash(f'Máquina "{nombre}" agregada exitosamente.', 'success')
            return redirect(url_for('listar_maquinas'))
        except IntegrityError: # Por si el nombre es único y ya existe
            db.rollback()
            flash(f'Error: Ya existe una máquina con el nombre "{nombre}".', 'danger')
        except Exception as e:
            db.rollback()
            flash(f'Error al agregar la máquina: {e}', 'danger')
     
    return render_template('maquina_form.html', maquina={}, action='nueva', titulo='Agregar Nueva Máquina')

@app.route('/maquinas/editar/<int:id_maquina>', methods=['GET', 'POST'])
def editar_maquina(id_maquina):
    db = next(get_db())
    maquina_a_editar = db.query(MaquinaInyectora).get(id_maquina)

    if not maquina_a_editar:
        flash('Máquina no encontrada.', 'danger')
        db.close()
        return redirect(url_for('listar_maquinas'))

    if request.method == 'POST':
        maquina_a_editar.nombre = request.form.get('nombre')
        costo_hora_str = request.form.get('costo_hora')
        tiempo_cambio_molde_horas_str = request.form.get('tiempo_cambio_molde_horas')
        maquina_a_editar.planta = request.form.get('planta')

        if not maquina_a_editar.nombre or not costo_hora_str or not tiempo_cambio_molde_horas_str:
            flash('Nombre, costo por hora y tiempo de cambio de molde son obligatorios.', 'danger')
            # Pasamos maquina_a_editar para repoblar el formulario con los datos actuales (o los que se intentaron guardar)
            return render_template('maquina_form.html', maquina=maquina_a_editar, action='editar', id_maquina=id_maquina, titulo=f'Editar Máquina: {maquina_a_editar.nombre}')

        try:
            maquina_a_editar.costo_hora = float(costo_hora_str)
            maquina_a_editar.tiempo_cambio_molde_horas = float(tiempo_cambio_molde_horas_str)
            if maquina_a_editar.costo_hora < 0 or maquina_a_editar.tiempo_cambio_molde_horas < 0:
                 raise ValueError("Los valores numéricos no pueden ser negativos.")
        except ValueError as e:
            flash(f'Error en los valores numéricos: {e}', 'danger')
            return render_template('maquina_form.html', maquina=maquina_a_editar, action='editar', id_maquina=id_maquina, titulo=f'Editar Máquina: {maquina_a_editar.nombre}')

        try:
            db.commit()
            flash(f'Máquina "{maquina_a_editar.nombre}" actualizada exitosamente.', 'success')
            db.close()
            return redirect(url_for('listar_maquinas'))
        except IntegrityError:
            db.rollback()
            flash(f'Error: Ya existe otra máquina con el nombre "{maquina_a_editar.nombre}".', 'danger')
        except Exception as e:
            db.rollback()
            flash(f'Error al actualizar la máquina: {e}', 'danger')
        # No cerramos la BD aquí si hay error y volvemos a renderizar el form, porque maquina_a_editar debe seguir siendo accesible

    db.close() # Cerrar la sesión si es GET o si la edición fue exitosa y se redirigió.
    return render_template('maquina_form.html', maquina=maquina_a_editar, action='editar', id_maquina=id_maquina, titulo=f'Editar Máquina: {maquina_a_editar.nombre}')

@app.route('/maquinas/eliminar/<int:id_maquina>', methods=['POST']) # Solo POST para eliminar
def eliminar_maquina(id_maquina):
    db = next(get_db())
    maquina_a_eliminar = db.query(MaquinaInyectora).get(id_maquina)
    if maquina_a_eliminar:
        try:
            db.delete(maquina_a_eliminar)
            db.commit()
            flash(f'Máquina "{maquina_a_eliminar.nombre}" eliminada.', 'success')
        except Exception as e:
            db.rollback()
            flash(f'Error al eliminar la máquina: {e}. Asegúrate de que no tenga artículos asociados.', 'danger')
    else:
        flash('Máquina no encontrada.', 'warning')
    db.close()
    return redirect(url_for('listar_maquinas'))

@app.route('/articulos')
def listar_articulos():
    db = next(get_db()) # Obtener la sesión
    try:
        articulos = db.query(Articulo).order_by(Articulo.nombre).all()
        return render_template('articulos_lista.html', articulos=articulos) # Renderizar plantilla con sesión activa
    finally:
        db.close() # Asegurar que la sesión se cierre

@app.route('/articulos/nuevo', methods=['GET', 'POST'])
def agregar_articulo():
    if request.method == 'POST':
        id_articulo = request.form.get('id') # Ahora el ID se ingresa manualmente
        nombre = request.form.get('nombre')

        if not id_articulo or not nombre:
            flash('ID y Nombre del artículo son obligatorios.', 'danger')
            return render_template('articulo_form.html', articulo=request.form, action='nuevo')

        # Validar la longitud del ID si es necesario (VARCHAR(20))
        if len(id_articulo) > 20:
             flash('El ID del artículo no puede exceder los 20 caracteres.', 'danger')
             return render_template('articulo_form.html', articulo=request.form, action='nuevo')

        nuevo_articulo = Articulo(
            id=id_articulo,
            nombre=nombre
        )
        db = next(get_db())
        try:
            db.add(nuevo_articulo)
            db.commit()
            flash(f'Artículo "{nombre}" (ID: {id_articulo}) agregado exitosamente.', 'success')
            return redirect(url_for('listar_articulos'))
        except IntegrityError: # Por si el ID o el nombre son únicos y ya existen
            db.rollback()
            # Intentar determinar si el error es por ID o por nombre
            existing_articulo_by_id = db.query(Articulo).filter_by(id=id_articulo).first()
            existing_articulo_by_nombre = db.query(Articulo).filter_by(nombre=nombre).first()
            if existing_articulo_by_id:
                 flash(f'Error: Ya existe un artículo con el ID "{id_articulo}".', 'danger')
            elif existing_articulo_by_nombre:
                 flash(f'Error: Ya existe un artículo con el nombre "{nombre}".', 'danger')
            else:
                 flash(f'Error de integridad al agregar el artículo.', 'danger') # Mensaje genérico si no se puede determinar
        except Exception as e:
            db.rollback()
            flash(f'Error al agregar el artículo: {e}', 'danger')
        

    return render_template('articulo_form.html', articulo={}, action='nuevo', titulo='Agregar Nuevo Artículo')

@app.route('/articulos/editar/<string:id_articulo>', methods=['GET', 'POST']) # Usamos string para el ID
def editar_articulo(id_articulo):
    db = next(get_db())
    articulo_a_editar = db.query(Articulo).get(id_articulo)

    if not articulo_a_editar:
        flash('Artículo no encontrado.', 'danger')
        db.close()
        return redirect(url_for('listar_articulos'))

    if request.method == 'POST':
        # El ID no se edita, solo el nombre
        articulo_a_editar.nombre = request.form.get('nombre')

        if not articulo_a_editar.nombre:
            flash('El Nombre del artículo es obligatorio.', 'danger')
            return render_template('articulo_form.html', articulo=articulo_a_editar, action='editar', id_articulo=id_articulo, titulo=f'Editar Artículo: {articulo_a_editar.nombre}')

        try:
            db.commit()
            flash(f'Artículo "{articulo_a_editar.nombre}" (ID: {id_articulo}) actualizado exitosamente.', 'success')
            db.close()
            return redirect(url_for('listar_articulos'))
        except IntegrityError: # Por si el nombre ya existe
            db.rollback()
            flash(f'Error: Ya existe otro artículo con el nombre "{articulo_a_editar.nombre}".', 'danger')
        except Exception as e:
            db.rollback()
            flash(f'Error al actualizar el artículo: {e}', 'danger')

    db.close()
    return render_template('articulo_form.html', articulo=articulo_a_editar, action='editar', id_articulo=id_articulo, titulo=f'Editar Artículo: {articulo_a_editar.nombre}')

@app.route('/articulos/eliminar/<string:id_articulo>', methods=['POST']) # Solo POST para eliminar
def eliminar_articulo(id_articulo):
    db = next(get_db())
    articulo_a_eliminar = db.query(Articulo).get(id_articulo)
    if articulo_a_eliminar:
        try:
            db.delete(articulo_a_eliminar)
            db.commit()
            flash(f'Artículo "{articulo_a_eliminar.nombre}" (ID: {id_articulo}) eliminado exitosamente.', 'success')
        except Exception as e:
            db.rollback()
            # Gracias a ondelete='CASCADE' en los modelos, las dependencias en MaquinaArticuloProduccion y Ordenes se manejan.
            flash(f'Error al eliminar el artículo: {e}', 'danger')
    else:
        flash('Artículo no encontrado.', 'warning')
    db.close()
    return redirect(url_for('listar_articulos'))

# --- Rutas para MaquinaArticuloProduccion ---

@app.route('/maquinas/<int:id_maquina>/produccion', methods=['GET', 'POST'])
def gestionar_produccion_maquina(id_maquina):
    # Obtener la sesión y asegurar su cierre con try...finally
    db = next(get_db())
    maquina = db.query(MaquinaInyectora).get(id_maquina)

    if not maquina:
        flash('Máquina no encontrada.', 'danger')
        db.close()
        return redirect(url_for('listar_maquinas'))

    if request.method == 'POST': # Para agregar una nueva configuración de producción
        articulo_id = request.form.get('articulo_id')
        tasa_produccion_hora_str = request.form.get('tasa_produccion_hora')
        tiempo_cambio_molde_horas_str = request.form.get('tiempo_cambio_molde_horas')
        molde_requerido = request.form.get('molde_requerido')

        if not articulo_id or not tasa_produccion_hora_str:
            flash('Artículo y Tasa de Producción son obligatorios.', 'danger')
        else:
            try:
                tasa_produccion_hora = float(tasa_produccion_hora_str)
                tiempo_cambio_molde_horas = float(tiempo_cambio_molde_horas_str) if tiempo_cambio_molde_horas_str else None

                if tasa_produccion_hora <= 0:
                    raise ValueError("La tasa de producción debe ser positiva.")
                if tiempo_cambio_molde_horas is not None and tiempo_cambio_molde_horas < 0:
                    raise ValueError("El tiempo de cambio de molde no puede ser negativo.")

                # Verificar si ya existe esta configuración
                existente = db.query(MaquinaArticuloProduccion).filter_by(maquina_id=id_maquina, articulo_id=articulo_id).first()
                if existente:
                    flash(f'El artículo {articulo_id} ya está configurado para esta máquina. Edítelo si es necesario.', 'warning')
                else:
                    nueva_config = MaquinaArticuloProduccion(
                        maquina_id=id_maquina,
                        articulo_id=articulo_id,
                        tasa_produccion_hora=tasa_produccion_hora,
                        tiempo_cambio_molde_horas=tiempo_cambio_molde_horas,
                        molde_requerido=molde_requerido if molde_requerido else None
                    )
                    db.add(nueva_config)
                    db.commit()
                    flash('Configuración de producción agregada exitosamente.', 'success')
                    # No cerramos db aquí, se cerrará al final de la función GET
                    return redirect(url_for('gestionar_produccion_maquina', id_maquina=id_maquina))

            except ValueError as e:
                flash(f'Error en los datos ingresados: {e}', 'danger')
            except IntegrityError:
                db.rollback()
                flash('Error de integridad. Es posible que el artículo no exista o ya esté configurado.', 'danger')
            except Exception as e:
                db.rollback()
                flash(f'Error al agregar configuración: {e}', 'danger')

    # GET request o si POST falló y necesita recargar la página
    # Las consultas se ejecutan DENTRO del bloque try, con la sesión activa
    configuraciones = db.query(MaquinaArticuloProduccion).filter_by(maquina_id=id_maquina).join(Articulo).order_by(Articulo.nombre).all()
    
    # Artículos disponibles para agregar (que aún no están configurados para esta máquina)
    articulos_configurados_ids = [config.articulo_id for config in configuraciones]
    articulos_disponibles = db.query(Articulo).filter(Articulo.id.notin_(articulos_configurados_ids)).order_by(Articulo.nombre).all()
    
    return render_template('maquina_produccion_config.html',
                           maquina=maquina,
                           configuraciones=configuraciones,
                           articulos_disponibles=articulos_disponibles)

@app.route('/maquinas/<int:id_maquina>/produccion/<string:id_articulo>/eliminar', methods=['POST'])
def eliminar_configuracion_produccion(id_maquina, id_articulo):
    db = next(get_db())
    config_a_eliminar = db.query(MaquinaArticuloProduccion).filter_by(maquina_id=id_maquina, articulo_id=id_articulo).first()

    if config_a_eliminar:
        try:
            db.delete(config_a_eliminar)
            db.commit()
            flash('Configuración de producción eliminada exitosamente.', 'success')
        except Exception as e:
            db.rollback()
            flash(f'Error al eliminar la configuración: {e}', 'danger')
    else:
        flash('Configuración de producción no encontrada.', 'warning')
    
    db.close()
    return redirect(url_for('gestionar_produccion_maquina', id_maquina=id_maquina))

@app.route('/maquinas/<int:id_maquina>/produccion/<string:id_articulo>/editar', methods=['GET', 'POST'])
def editar_configuracion_produccion(id_maquina, id_articulo):
    db = next(get_db())
    try:
        config_a_editar = db.query(MaquinaArticuloProduccion).filter_by(maquina_id=id_maquina, articulo_id=id_articulo).first()

        if not config_a_editar:
            flash('Configuración de producción no encontrada.', 'danger')
            return redirect(url_for('gestionar_produccion_maquina', id_maquina=id_maquina))

        maquina = db.query(MaquinaInyectora).get(id_maquina) # Para mostrar el nombre de la máquina

        if request.method == 'POST':
            tasa_produccion_hora_str = request.form.get('tasa_produccion_hora')
            tiempo_cambio_molde_horas_str = request.form.get('tiempo_cambio_molde_horas')
            molde_requerido = request.form.get('molde_requerido')

            if not tasa_produccion_hora_str:
                flash('La Tasa de Producción es obligatoria.', 'danger')
            else:
                try:
                    config_a_editar.tasa_produccion_hora = float(tasa_produccion_hora_str)
                    config_a_editar.tiempo_cambio_molde_horas = float(tiempo_cambio_molde_horas_str) if tiempo_cambio_molde_horas_str else None
                    config_a_editar.molde_requerido = molde_requerido if molde_requerido else None

                    if config_a_editar.tasa_produccion_hora <= 0:
                        raise ValueError("La tasa de producción debe ser positiva.")
                    if config_a_editar.tiempo_cambio_molde_horas is not None and config_a_editar.tiempo_cambio_molde_horas < 0:
                        raise ValueError("El tiempo de cambio de molde no puede ser negativo.")

                    db.commit()
                    flash('Configuración de producción actualizada exitosamente.', 'success')
                    return redirect(url_for('gestionar_produccion_maquina', id_maquina=id_maquina))

                except ValueError as e:
                    db.rollback()
                    flash(f'Error en los datos ingresados: {e}', 'danger')
                except Exception as e:
                    db.rollback()
                    flash(f'Error al actualizar la configuración: {e}', 'danger')
        
        # Para GET request o si el POST falló y necesita recargar el formulario
        return render_template('maquina_produccion_edit_form.html',
                               config=config_a_editar,
                               maquina=maquina)
    finally:
        db.close()

# --- Rutas para Órdenes ---

@app.route('/ordenes')
def listar_ordenes():
    db = next(get_db())
    try:
        # Hacemos join con Articulo para poder mostrar el nombre del artículo y ordenamos
        ordenes = db.query(Orden).join(Articulo).order_by(Orden.fecha_entrega_deseada, Articulo.nombre).all()
        return render_template('ordenes_lista.html', ordenes=ordenes)
    finally:
        db.close()

@app.route('/ordenes/nueva', methods=['GET', 'POST'])
def agregar_orden():
    db = next(get_db())
    try:
        if request.method == 'POST':
            codigo_pedido = request.form.get('codigo_pedido')
            articulo_id = request.form.get('articulo_id')
            cantidad_str = request.form.get('cantidad')
            fecha_entrega_deseada_str = request.form.get('fecha_entrega_deseada')

            if not codigo_pedido or not articulo_id or not cantidad_str:
                flash('Código de Pedido, Artículo y Cantidad son obligatorios.', 'danger')
            else:
                try:
                    cantidad = int(cantidad_str) # type: ignore
                    if cantidad <= 0:
                        raise ValueError("La cantidad debe ser positiva.")

                    fecha_entrega_deseada = None
                    if fecha_entrega_deseada_str:
                        # Asumiendo formato YYYY-MM-DD para el input date
                        from datetime import datetime
                        fecha_entrega_deseada = datetime.strptime(fecha_entrega_deseada_str, '%Y-%m-%d').date()

                    nueva_orden = Orden(
                        codigo_pedido=codigo_pedido,
                        articulo_id=articulo_id,
                        cantidad=cantidad,
                        fecha_entrega_deseada=fecha_entrega_deseada
                    )
                    db.add(nueva_orden)
                    db.commit()
                    flash('Orden agregada exitosamente.', 'success')
                    return redirect(url_for('listar_ordenes'))
                except ValueError as e:
                    db.rollback()
                    flash(f'Error en los datos ingresados: {e}', 'danger')
                except IntegrityError: # Principalmente por FK de articulo_id
                    db.rollback()
                    flash('Error de integridad: Verifique que el artículo seleccionado exista.', 'danger')
                except Exception as e:
                    db.rollback()
                    flash(f'Error al agregar la orden: {e}', 'danger')
        
        # Para GET request o si el POST falló
        articulos = db.query(Articulo).order_by(Articulo.nombre).all()
        return render_template('orden_form.html', orden={}, articulos=articulos, action='nueva', titulo='Agregar Nueva Orden')
    finally:
        db.close()

@app.route('/ordenes/editar/<int:id_orden>', methods=['GET', 'POST'])
def editar_orden(id_orden):
    db = next(get_db())
    try:
        orden_a_editar = db.query(Orden).get(id_orden)
        if not orden_a_editar:
            flash('Orden no encontrada.', 'danger')
            return redirect(url_for('listar_ordenes'))

        if request.method == 'POST':
            # codigo_pedido y articulo_id no se editan.
            # Solo cantidad y fecha_entrega_deseada.
            orden_a_editar.cantidad = int(request.form.get('cantidad')) # type: ignore
            fecha_entrega_deseada_str = request.form.get('fecha_entrega_deseada')
            
            if orden_a_editar.cantidad <= 0:
                flash('La cantidad debe ser positiva.', 'danger')
            else:
                if fecha_entrega_deseada_str:
                    from datetime import datetime
                    orden_a_editar.fecha_entrega_deseada = datetime.strptime(fecha_entrega_deseada_str, '%Y-%m-%d').date()
                else:
                    orden_a_editar.fecha_entrega_deseada = None
                
                try:
                    db.commit()
                    flash('Orden actualizada exitosamente.', 'success')
                    return redirect(url_for('listar_ordenes'))
                except Exception as e:
                    db.rollback()
                    flash(f'Error al actualizar la orden: {e}', 'danger')

        articulos = db.query(Articulo).order_by(Articulo.nombre).all() # Necesario para el selector si se permite cambiar artículo
        return render_template('orden_form.html', orden=orden_a_editar, articulos=articulos, action='editar', titulo=f'Editar Orden: {orden_a_editar.codigo_pedido}')
    finally:
        db.close()

@app.route('/ordenes/eliminar/<int:id_orden>', methods=['POST'])
def eliminar_orden(id_orden):
    db = next(get_db())
    try:
        orden_a_eliminar = db.query(Orden).get(id_orden)
        if not orden_a_eliminar:
            flash('Orden no encontrada.', 'warning')
        else:
            db.delete(orden_a_eliminar)
            db.commit()
            flash(f'Orden Código: {orden_a_eliminar.codigo_pedido} (ID: {id_orden}) eliminada exitosamente.', 'success')
    except Exception as e:
        db.rollback()
        flash(f'Error al eliminar la orden: {e}', 'danger')
    finally:
        db.close()
    return redirect(url_for('listar_ordenes'))

# --- Rutas para Planificación ---

@app.route('/planificar', methods=['GET', 'POST'])
def planificar():
    db = next(get_db())
    gantt_image_url = None 
    try:
        if request.method == 'POST':
            # --- Lógica para iniciar la planificación ---
            ordenes_ids_seleccionadas_str = request.form.getlist('ordenes_seleccionadas')
            maquinas_ids_seleccionadas_str = request.form.getlist('maquinas_seleccionadas')

            # Convertir IDs a tipos correctos (Integer para órdenes, Integer para máquinas)
            try:
                ordenes_ids = [int(id) for id in ordenes_ids_seleccionadas_str]
                maquinas_ids = [int(id) for id in maquinas_ids_seleccionadas_str]
            except ValueError:
                flash('Error en los IDs seleccionados.', 'danger')
                # Recargar la página con los datos actuales
                ordenes_pendientes = db.query(Orden).order_by(Orden.fecha_entrega_deseada).all()
                maquinas_disponibles = db.query(MaquinaInyectora).order_by(MaquinaInyectora.nombre).all()
                resultados_anteriores = db.query(PlanificacionResultado).order_by(PlanificacionResultado.tiempo_inicio.desc()).limit(10).all()
                return render_template('planificar.html',
                                       ordenes_pendientes=ordenes_pendientes,
                                       maquinas_disponibles=maquinas_disponibles,
                                       resultados_anteriores=resultados_anteriores)

            # Obtener los objetos de la base de datos
            ordenes_a_planificar = db.query(Orden).filter(Orden.id.in_(ordenes_ids)).all()
            maquinas_a_usar = db.query(MaquinaInyectora).filter(MaquinaInyectora.id.in_(maquinas_ids)).all()

            if not ordenes_a_planificar:
                flash('No se seleccionaron órdenes para planificar.', 'warning')
            elif not maquinas_a_usar:
                flash('No se seleccionaron máquinas disponibles.', 'warning')
            else:
                # --- Llamada al algoritmo de planificación ---
                resultados = run_planning(db, ordenes_a_planificar, maquinas_a_usar)

                if resultados:
                    flash(f'Planificación ejecutada. Se generaron {len(resultados)} asignaciones.', 'success')
                else:
                    flash('Planificación ejecutada, pero no se generaron asignaciones. Verifique la compatibilidad y configuración.', 'warning')
                    print("DEBUG app_planif: No se generaron resultados de planificación, no se llamará a generar_gantt_chart.")
            # Después de ejecutar el algoritmo y guardar los resultados en PlanificacionResultado,
            # la lógica común de abajo recuperará los resultados (incluyendo los nuevos) y generará el Gantt.

        # --- Lógica común para GET y para después de POST ---
        # Mostrar órdenes pendientes, máquinas disponibles, y resultados anteriores
        ordenes_pendientes = db.query(Orden).order_by(Orden.fecha_entrega_deseada).all()
        maquinas_disponibles = db.query(MaquinaInyectora).order_by(MaquinaInyectora.nombre).all()
        
        # Recuperar resultados de planificación anteriores (podríamos añadir filtros después)
        resultados_anteriores = db.query(PlanificacionResultado).order_by(PlanificacionResultado.tiempo_inicio.desc()).limit(10).all() # Mostrar los últimos 10 resultados
        
        if resultados_anteriores:
            # Crear un diccionario de máquinas para pasar al generador de Gantt
            maquinas_dict_for_gantt = {m.id: m for m in db.query(MaquinaInyectora).all()}
            plots_dir = os.path.join(app.static_folder, 'plots')
            gantt_image_url = generar_gantt_chart(resultados_anteriores, maquinas_dict_for_gantt, output_folder=plots_dir)
            print(f"DEBUG app_planif: gantt_image_url generado a partir de resultados_anteriores: {gantt_image_url}")
        else:
            print("DEBUG app_planif: No hay resultados_anteriores para generar Gantt.")

        print(f"DEBUG app_planif: Pasando gantt_image_url a la plantilla: {gantt_image_url}")

        return render_template('planificar.html',
                               ordenes_pendientes=ordenes_pendientes,
                               maquinas_disponibles=maquinas_disponibles,
                           resultados_anteriores=resultados_anteriores,
                           gantt_image_url=gantt_image_url) # Pasar la URL a la plantilla
    finally:
        db.close()

@app.route('/planificacion/resultado/eliminar/<int:id_resultado>', methods=['POST'])
def eliminar_resultado_planificacion(id_resultado):
    db = next(get_db())
    try:
        resultado_a_eliminar = db.query(PlanificacionResultado).get(id_resultado)
        if not resultado_a_eliminar:
            flash('Resultado de planificación no encontrado.', 'warning')
        else:
            db.delete(resultado_a_eliminar)
            db.commit()
            flash(f'Resultado de planificación ID: {id_resultado} eliminado exitosamente.', 'success')
    except Exception as e:
        db.rollback()
        flash(f'Error al eliminar el resultado de planificación: {e}', 'danger')
    finally:
        db.close()
    return redirect(url_for('planificar')) # Redirigir de vuelta a la página de planificación


if __name__ == '__main__':
    app.run(debug=True, port=5001) # Usar un puerto diferente si la otra app Flask está en 5000