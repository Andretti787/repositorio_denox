from flask import Flask, render_template, request, redirect, url_for, session, flash, Response
import pyodbc
from datetime import datetime
from zoneinfo import ZoneInfo

app = Flask(__name__)
app.secret_key = 'clave_secreta_mes_famesa'

DB_CONFIG = (
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=192.168.28.100,51439;"
    "DATABASE=x3;"
    "UID=it;"
    "PWD=@Famesa123;"
)

# Configuración de zona horaria local
SPAIN_TZ = ZoneInfo("Europe/Madrid")

def get_db_connection():
    try:
        return pyodbc.connect(DB_CONFIG)
    except Exception:
        return None

@app.route('/', methods=['GET', 'POST'])
def config_equipo():
    equipos_fijos = ['397', '398', '399']
    if request.method == 'POST':
        session['equipo'] = request.form.get('equipo')
        return redirect(url_for('config_turno'))
    return render_template('setup_step.html', titulo="Equipo de Trabajo", campo="equipo", choices=equipos_fijos)

@app.route('/turno', methods=['GET', 'POST'])
def config_turno():
    turnos_fijos = ['MAÑANA', 'TARDE', 'NOCHE']
    if request.method == 'POST':
        session['turno'] = request.form.get('turno')
        return redirect(url_for('config_maquina'))
    return render_template('setup_step.html', titulo="Turno", campo="turno", choices=turnos_fijos)

@app.route('/maquina', methods=['GET', 'POST'])
def config_maquina():
    if request.method == 'POST':
        session['maquina'] = request.form.get('maquina')
        return redirect(url_for('seleccionar_of'))
    return render_template('setup_step.html', titulo="Máquina", campo="maquina", choices=None) # No hay choices para máquina, sigue siendo texto libre

@app.route('/seleccionar_of')
def seleccionar_of():
    maquina = session.get('maquina')
    conn = get_db_connection()
    ofs = []
    active_timers = {}
    if conn:
        cursor = conn.cursor()
        # Obtener OFs disponibles
        query = """
            SELECT DISTINCT MFO.MFGNUM_0, MFG.ROUNUM_0, ITM.ITMDES1_0, MFO.PLNFCY_0, MFO.STDLAB_0
            FROM LIVE.MFGOPE MFO
            INNER JOIN LIVE.MFGHEAD MFG ON MFO.MFGNUM_0 = MFG.MFGNUM_0
            INNER JOIN LIVE.ITMMASTER ITM ON ITM.ITMREF_0 = MFG.ROUNUM_0
            WHERE MFO.STDWST_0 = ? AND MFO.MFGSTA_0 = 1 AND MFG.RMNEXTQTY_0 > 0
        """
        cursor.execute(query, (maquina,))
        ofs = cursor.fetchall()
        
        # Obtener todos los timers activos para mostrarlos en la lista
        cursor.execute("SELECT MFGNUM, DESC_OP FROM LIVE.ZMES_TIMERS_ACTIVOS")
        active_timers = {row[0]: {'desc_op': row[1]} for row in cursor.fetchall()}
        
        conn.close()
    return render_template('seleccionar_of.html', ofs=ofs, active_timers=active_timers)

@app.route('/seguimiento/<of_num>', methods=['GET', 'POST'])
def seguimiento_of(of_num):
    # Verificación de seguridad: asegurar que existen datos en sesión
    if not all(k in session for k in ['equipo', 'turno', 'maquina']): # Verifica si todas las claves necesarias están en la sesión
        flash("Debes completar la configuración inicial", "warning") 
        return redirect(url_for('config_equipo'))

    conn = get_db_connection()
    if not conn: return "Error de DB", 500
    cursor = conn.cursor()

    try:
        # Consultar si hay un timer activo en BD para esta OF
        cursor.execute("SELECT START_TIME, COD_OP, DESC_OP FROM LIVE.ZMES_TIMERS_ACTIVOS WHERE MFGNUM = ?", (of_num,))
        res_timer = cursor.fetchone()
        timer_data = {
            'start_time': res_timer[0].replace(tzinfo=SPAIN_TZ).isoformat(), 'cod_op': res_timer[1], 'desc_op': res_timer[2]
        } if res_timer else None

        # Obtener Planta y Máquina de mano de obra para el registro
        cursor.execute("SELECT PLNFCY_0, STDLAB_0 FROM LIVE.MFGOPE WHERE MFGNUM_0 = ? AND STDWST_0 = ?", (of_num, session['maquina']))
        ope_info = cursor.fetchone()
        planta = ope_info[0] if ope_info else ""
        stdlab = ope_info[1] if ope_info else ""

        if request.method == 'POST':
            action = request.form.get('action')
            selected_cod_op = request.form.get('cod_operacion')

            if action == 'start_timed':
                cursor.execute("SELECT TEXTE_0 FROM LIVE.ATEXTRA WHERE CODFIC_0 = 'TABMSG' AND ZONE_0 = 'MSGDESAXX' AND LANGUE_0 = 'SPA' AND IDENT1_0 = ?", (selected_cod_op,))
                res_desc = cursor.fetchone()
                desc_op = res_desc[0] if res_desc else selected_cod_op

                # Persistir en base de datos
                cursor.execute("""
                    INSERT INTO LIVE.ZMES_TIMERS_ACTIVOS (MFGNUM, EQUIPO, TURNO, MAQUINA, COD_OP, DESC_OP, START_TIME)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """, (of_num, session['equipo'], session['turno'], session['maquina'], selected_cod_op, desc_op, datetime.now(SPAIN_TZ)))
                conn.commit()
                
                flash("Operación iniciada. El tiempo se está contabilizando.", "info")
                return redirect(url_for('seguimiento_of', of_num=of_num))

            elif action == 'register_injection':
                p_buenas = int(request.form.get('piezas_buenas', 0))
                p_malas = int(request.form.get('piezas_malas', 0))

                cursor.execute("SELECT SUM(PIEZAS_OK) FROM LIVE.ZMES_SEGUIMIENTO WHERE MFGNUM = ?", (of_num,))
                piezas_acumuladas = int(cursor.fetchone()[0] or 0)
                
                cursor.execute("SELECT EXTQTY_0 FROM LIVE.MFGHEAD WHERE MFGNUM_0 = ?", (of_num,))
                res_of = cursor.fetchone()
                if not res_of:
                    flash("Error: No se ha encontrado la OF en el sistema.", "danger")
                    return redirect(url_for('seleccionar_of'))
                
                pedido = int(res_of[0])
                
                if (piezas_acumuladas + p_buenas) > pedido:
                    flash(f"Error: La suma de piezas BUENAS ({piezas_acumuladas + p_buenas}) superaría el pedido ({pedido}).", "danger")
                    return redirect(url_for('seguimiento_of', of_num=of_num))

                query_insert = "INSERT INTO LIVE.ZMES_SEGUIMIENTO (FECHA, EQUIPO, TURNO, MAQUINA, MFGNUM, COD_OP, TIEMPO_OP, PIEZAS_OK, PIEZAS_KO, USUARIO, PLANTA, STDLAB) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                cursor.execute(query_insert, (datetime.now(SPAIN_TZ), session['equipo'], session['turno'], session['maquina'], of_num, '0', 0.0, p_buenas, p_malas, 'it', planta, stdlab))
                conn.commit()
                flash(f"Producción de inyección registrada con éxito", "success")
                return redirect(url_for('seguimiento_of', of_num=of_num))

            elif action == 'register_timed':
                if not timer_data:
                    flash("Error: No hay una operación activa.", "warning")
                    return redirect(url_for('seguimiento_of', of_num=of_num))

                start_time = datetime.fromisoformat(timer_data['start_time'])
                end_time = datetime.now(SPAIN_TZ)
                duracion_segundos = (end_time - start_time).total_seconds()
                tiempo_op = round(duracion_segundos / 3600, 2)
                
                cod_op = timer_data['cod_op']

                cursor.execute("SELECT SUM(TIEMPO_OP) FROM LIVE.ZMES_SEGUIMIENTO WHERE MFGNUM = ?", (of_num,))
                tiempo_acumulado = float(cursor.fetchone()[0] or 0)
                if (tiempo_acumulado + tiempo_op) > 24:
                    flash(f"Aviso: El tiempo total acumulado ({tiempo_acumulado + tiempo_op}h) no puede superar las 24 horas.", "warning")
                    return redirect(url_for('seguimiento_of', of_num=of_num))

                query_insert = """
                    INSERT INTO LIVE.ZMES_SEGUIMIENTO 
                    (FECHA, EQUIPO, TURNO, MAQUINA, MFGNUM, COD_OP, TIEMPO_OP, PIEZAS_OK, PIEZAS_KO, USUARIO, PLANTA, STDLAB)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
                valores = (datetime.now(SPAIN_TZ), session['equipo'], session['turno'], session['maquina'], 
                           of_num, cod_op, tiempo_op, 0, 0, 'it', planta, stdlab)
                
                cursor.execute(query_insert, valores)
                conn.commit()

                # Eliminar de timers activos
                cursor.execute("DELETE FROM LIVE.ZMES_TIMERS_ACTIVOS WHERE MFGNUM = ?", (of_num,))
                conn.commit()
                
                flash(f"Seguimiento de la OF {of_num} registrado con éxito", "success")
                return redirect(url_for('seguimiento_of', of_num=of_num))

            elif action == 'cancel':
                cursor.execute("DELETE FROM LIVE.ZMES_TIMERS_ACTIVOS WHERE MFGNUM = ?", (of_num,))
                conn.commit()
                flash("Operación cancelada.", "secondary")
                return redirect(url_for('seguimiento_of', of_num=of_num))

        # Lógica GET: Obtener datos para la vista
        cursor.execute("SELECT IDENT1_0, TEXTE_0 FROM LIVE.ATEXTRA WHERE CODFIC_0 = 'TABMSG' AND ZONE_0 = 'MSGDESAXX' AND LANGUE_0 = 'SPA'")
        operaciones = cursor.fetchall()

        cursor.execute("SELECT MFG.ROUNUM_0, ITM.ITMDES1_0, MFG.MFGNUM_0, MFG.EXTQTY_0, MFG.CPLQTY_0, MFO.BASQTY_0, MFO.EXTOPETIM_0 fROM LIVE.MFGHEAD MFG INNER JOIN LIVE.ITMMASTER ITM ON ITM.ITMREF_0 = MFG.ROUNUM_0 LEFT JOIN LIVE.MFGOPE MFO ON MFO.MFGNUM_0 = MFG.MFGNUM_0 WHERE MFG.MFGNUM_0 = ?", (of_num,))
        detalle = cursor.fetchone()
        
        cursor.execute("SELECT MAT.ITMREF_0, ITM.ITMDES1_0, MAT.RETQTY_0, MAT.STU_0 fROM LIVE.MFGMAT MAT LEFT JOIN LIVE.ITMMASTER ITM ON ITM.ITMREF_0 = MAT.ITMREF_0 WHERE MFGNUM_0 = ?", (of_num,))
        materiales = cursor.fetchall()

        query_historial = """
            SELECT Z.FECHA, COALESCE(A.TEXTE_0, Z.COD_OP), Z.TIEMPO_OP, Z.PIEZAS_OK, Z.PIEZAS_KO 
            FROM LIVE.ZMES_SEGUIMIENTO Z 
            LEFT JOIN LIVE.ATEXTRA A ON A.IDENT1_0 = Z.COD_OP 
                AND A.CODFIC_0 = 'TABMSG' 
                AND A.ZONE_0 = 'MSGDESAXX' 
                AND A.LANGUE_0 = 'SPA' 
            WHERE Z.MFGNUM = ? 
            ORDER BY Z.FECHA DESC
        """
        cursor.execute(query_historial, (of_num,))
        historial = cursor.fetchall()

        # Cálculo de totales para el historial
        total_tiempo = sum(float(row[2] or 0) for row in historial)
        total_buenas = sum(int(row[3] or 0) for row in historial)
        total_malas = sum(int(row[4] or 0) for row in historial)

        return render_template('detalle_of.html', of=of_num, detalle=detalle, materiales=materiales, operaciones=operaciones, historial=historial, timer=timer_data, total_tiempo=total_tiempo, total_buenas=total_buenas, total_malas=total_malas)

    except Exception as e:
        flash(f"Error en el sistema: {str(e)}", "danger")
        return redirect(url_for('seleccionar_of'))
    finally:
        conn.close()

@app.route('/exportar_historial/<of_num>')
def exportar_historial(of_num):
    conn = get_db_connection()
    if not conn: return "Error de DB", 500
    cursor = conn.cursor()
    try:
        # Formato: PLANTA;OF;USUARIO;MÁQUINA;MÁQUINA DE MANO DE OBRA;PIEZAS BUENAS;PIEZAS MALAS;TIEMPO DE SEGUMIENTO;'ID_EMPLEADO';FECHA;'ZPOI';PIEZAS MALAS;CODIGO DE OPERACIÓN
        query = "SELECT PLANTA, MFGNUM, USUARIO, MAQUINA, STDLAB, PIEZAS_OK, PIEZAS_KO, TIEMPO_OP, FECHA, COD_OP FROM LIVE.ZMES_SEGUIMIENTO WHERE MFGNUM = ? ORDER BY FECHA DESC"
        cursor.execute(query, (of_num,))
        rows = cursor.fetchall()
        
        output = []
        for r in rows:
            fecha_str = r[8].strftime('%d%m%y') if r[8] else ""
            tiempo_str = str(r[7]) if r[7] is not None else "0.00"
            line = f"{r[0]};{r[1]};{r[2]};{r[3]};{r[4]};{r[5]};{r[6]};{tiempo_str};ID_EMPLEADO;{fecha_str};ZPOI;{r[6]};{r[9]}"
            output.append(line)
        
        content = "\n".join(output)
        return Response(content, mimetype="text/plain", headers={"Content-disposition": f"attachment; filename=historial_{of_num}.txt"})
    except Exception as e:
        return f"Error al exportar: {str(e)}", 500
    finally:
        conn.close()

if __name__ == '__main__':
    app.run(debug=True, port=5004)