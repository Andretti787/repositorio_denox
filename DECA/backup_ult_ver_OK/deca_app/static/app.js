// Cliente web DeCA: filtro por origen, ordenacion por columnas y sombreado de generados.
const cuerpoTabla = document.getElementById("cuerpo-tabla");
const chkPendientes = document.getElementById("chk-pendientes");
const chkTodos = document.getElementById("chk-todos");
const divMensajes = document.getElementById("mensajes");
const numLimite = document.getElementById("num-limite");
const txtFiltro = document.getElementById("txt-filtro");
const selOrigen = document.getElementById("sel-origen");
const txtTransportista = document.getElementById("txt-transportista");
const divResumen = document.getElementById("resumen-seleccion");
const panelManual = document.getElementById("panel-manual");
const selManOrigen = document.getElementById("man-origen");
const txtManFecha = document.getElementById("man-fecha");
const txtManReferencia = document.getElementById("man-referencia");
const txtManTransportista = document.getElementById("man-transportista");
const txtManTransportistaNif = document.getElementById("man-transportista-nif");
const txtManMatTractor = document.getElementById("man-mat-tractor");
const txtManMatRemolque = document.getElementById("man-mat-remolque");
const txtManPeso = document.getElementById("man-peso");
const txtManPalets = document.getElementById("man-palets");
const txtManDestino = document.getElementById("man-destino");
const txtManObservaciones = document.getElementById("man-observaciones");
const divManCargador = document.getElementById("man-cargador");
const btnManGenerar = document.getElementById("btn-manual-generar");

let ordenarPor = "fecha_transporte";
let ordenDesc = true;

// Sin validacion de formato de matricula: se admiten espanolas y extranjeras.

function normalizar(valor) {
    return String(valor || "").trim().toUpperCase();
}

function esc(texto) {
    return String(texto === null || texto === undefined ? "" : texto)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;")
        .replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

// Destino tal y como viene del albaran en Sage (CP + ciudad + pais)
function destinoAutomatico(a) {
    const cp = String(a.destino_cp || "").trim();
    const ciudad = String(a.destino_ciudad || "").trim();
    const pais = String(a.destino_pais || "").trim();
    const partes = [];
    if (cp) partes.push(cp);
    if (ciudad) partes.push(ciudad);
    if (pais) partes.push("(" + pais + ")");
    return partes.join(" ");
}

// Destino efectivo: el editado manualmente si existe; si no, el del albaran
function formatearDestino(a) {
    return String(a.destino || "").trim() || destinoAutomatico(a);
}

function mostrarMensaje(texto, esError) {
    divMensajes.textContent = texto;
    divMensajes.className = esError ? "mensaje error" : "mensaje ok";
    if (!esError) {
        setTimeout(function () {
            divMensajes.textContent = "";
            divMensajes.className = "mensaje";
        }, 12000);
    }
}

async function pedirJson(url, opciones) {
    const respuesta = await fetch(url, opciones);
    if (!respuesta.ok) {
        let detalle = "HTTP " + respuesta.status;
        try {
            const cuerpo = await respuesta.json();
            if (cuerpo && cuerpo.detail) {
                detalle = typeof cuerpo.detail === "string"
                    ? cuerpo.detail : JSON.stringify(cuerpo.detail);
            }
        } catch (e) { /* sin JSON */ }
        throw new Error(detalle);
    }
    if (respuesta.status === 204) return null;
    return respuesta.json();
}

function actualizarIndicadoresOrden() {
    document.querySelectorAll("th.ordenable").forEach(function (th) {
        th.classList.remove("ord-asc", "ord-desc");
        if (th.dataset.campo === ordenarPor) {
            th.classList.add(ordenDesc ? "ord-desc" : "ord-asc");
        }
    });
}

async function cargarOrigenes() {
    try {
        const origenes = await pedirJson(
            "/albaranes/origenes?solo_pendientes=" + chkPendientes.checked
        );
        const actual = selOrigen.value;
        selOrigen.innerHTML = "<option value=\"\">Todos</option>";
        (origenes || []).forEach(function (o) {
            const opt = document.createElement("option");
            opt.value = o;
            opt.textContent = o;
            selOrigen.appendChild(opt);
        });
        if (actual) {
            selOrigen.value = actual;
            if (selOrigen.value !== actual) selOrigen.value = "";
        }
    } catch (e) {
        console.warn("No se pudieron cargar los origenes:", e.message);
    }
}

async function cargarAlbaranes() {
    cuerpoTabla.innerHTML = "<tr><td colspan=\"14\">Cargando albaranes...</td></tr>";
    try {
        const parametros = new URLSearchParams({
            solo_pendientes: chkPendientes.checked,
            limite: numLimite.value || 200,
            filtro: txtFiltro.value.trim(),
            origen: selOrigen.value || "",
            transportista: txtTransportista.value.trim(),
            ordenar_por: ordenarPor,
            orden_desc: ordenDesc
        });
        const albaranes = await pedirJson("/albaranes?" + parametros.toString());
        pintarTabla(albaranes);
        actualizarIndicadoresOrden();
        mostrarMensaje("Mostrando " + albaranes.length + " albaran(es).");
    } catch (e) {
        cuerpoTabla.innerHTML = "<tr><td colspan=\"14\">Error al cargar los albaranes</td></tr>";
        mostrarMensaje("Error al cargar los albaranes: " + e.message, true);
    }
}

function celdaInput(albaran, campo, extraClass, maxLen) {
    const cls = extraClass ? ("matricula " + extraClass) : "matricula";
    const ml = maxLen || 15;
    return "<td><input class=\"" + cls + "\" data-campo=\"" + campo
        + "\" maxlength=\"" + ml + "\" value=\"" + esc(albaran[campo]) + "\"></td>";
}

function celdaNumero(albaran, campo) {
    const valor = albaran[campo] == null ? "" : albaran[campo];
    return "<td><input class=\"numero editable-num\" data-campo=\"" + campo
        + "\" value=\"" + esc(valor) + "\"></td>";
}

// Destino editable: se muestra el destino efectivo y se recuerda el automatico
// en data-auto, para que el backend no fije una modificacion innecesaria.
function celdaDestino(albaran) {
    const efectivo = formatearDestino(albaran);
    const automatico = destinoAutomatico(albaran);
    return "<td><input class=\"texto editable-texto\" data-campo=\"destino\""
        + " data-auto=\"" + esc(automatico) + "\" maxlength=\"160\""
        + " title=\"Edite el destino solo si es necesario; dejelo vacio para usar el del albaran\""
        + " value=\"" + esc(efectivo) + "\"></td>";
}

// Celda de texto editable generica (observaciones, transportista, NIF...)
function celdaTextoEditable(albaran, campo, clase, maxLen, marcador, ayuda) {
    const valor = String(albaran[campo] || "");
    return "<td><input class=\"texto editable-texto " + clase + "\" data-campo=\"" + campo + "\""
        + " maxlength=\"" + maxLen + "\""
        + (marcador ? " placeholder=\"" + marcador + "\"" : "")
        + (ayuda ? " title=\"" + ayuda + "\"" : "")
        + " value=\"" + esc(valor) + "\"></td>";
}

// Observaciones editables por albaran: se imprimen en el DeCA
function celdaObservaciones(albaran) {
    return celdaTextoEditable(albaran, "observaciones", "editable-obs", 320, "Observaciones",
        "Observaciones que se imprimen en el DeCA");
}

// Transportista efectivo editable (si se deja vacio se usa el del albaran)
function celdaTransportista(albaran) {
    return celdaTextoEditable(albaran, "transportista_nombre", "editable-transp", 80,
        "Transportista", "Transportista efectivo que figura en el DeCA");
}

// NIF del transportista efectivo (editable)
function celdaTransportistaNif(albaran) {
    return celdaTextoEditable(albaran, "transportista_nif", "editable-transp-nif", 20,
        "NIF", "NIF del transportista efectivo");
}

function pintarTabla(albaranes) {
    if (!albaranes.length) {
        cuerpoTabla.innerHTML = "<tr><td colspan=\"14\">No hay albaranes que mostrar</td></tr>";
        return;
    }
    const vistos = {};
    const unicos = [];
    albaranes.forEach(function (a) {
        if (!a || !a.numalbaran || vistos[a.numalbaran]) return;
        vistos[a.numalbaran] = true;
        unicos.push(a);
    });
    cuerpoTabla.innerHTML = unicos.map(function (a) {
        const generado = a.deca_estado === 1;
        const estado = generado ? "Generado" : "Pendiente";
        const acciones = generado
            ? "<button type=\"button\" data-accion=\"generar\" class=\"primario\">Regenerar DeCA</button> "
              + "<a href=\"/albaranes/" + encodeURIComponent(a.numalbaran)
              + "/deca/pdf\" target=\"_blank\">Descargar</a> "
              + "<button type=\"button\" data-accion=\"email\">Enviar email</button>"
            : "<button type=\"button\" data-accion=\"generar\" class=\"primario\">Generar DeCA</button>";
        const peso = (a.peso_kg == null) ? "" : Number(a.peso_kg).toFixed(2);
        const palets = (a.num_pallets == null) ? "" : String(a.num_pallets);
        const dest = formatearDestino(a);
        return "<tr data-sdhnum=\"" + esc(a.numalbaran) + "\""
            + " class=\"" + (generado ? "fila-generado" : "fila-pendiente") + "\""
            + " data-peso=\"" + esc(peso) + "\""
            + " data-palets=\"" + esc(palets) + "\""
            + " data-destino=\"" + esc(dest) + "\""
            + " data-transportista=\"" + esc(a.transportista_codigo || "") + "\">"
            + "<td><input type=\"checkbox\" class=\"seleccion\"></td>"
            + "<td>" + esc(a.numalbaran) + "</td>"
            + "<td>" + esc(a.fecha_transporte) + "</td>"
            + celdaTransportista(a)
            + celdaTransportistaNif(a)
            + "<td>" + esc(a.origen) + "</td>"
            + celdaDestino(a)
            + celdaNumero(a, "peso_kg")
            + celdaNumero(a, "num_pallets")
            + celdaObservaciones(a)
            + celdaInput(a, "matricula_tractor")
            + celdaInput(a, "matricula_remolque")
            + "<td class=\"" + (generado ? "estado-ok" : "estado-pendiente") + "\">" + estado + "</td>"
            + "<td class=\"acciones\">" + acciones + "</td>"
            + "</tr>";
    }).join("");
}

function leerMatriculas(fila) {
    const datos = {};
    fila.querySelectorAll("input.matricula, input.editable-num, input.editable-texto")
        .forEach(function (input) {
            const campo = input.dataset.campo;
            if (campo === "matricula_tractor" || campo === "matricula_remolque") {
                datos[campo] = normalizar(input.value) || null;
            } else if (campo === "peso_kg") {
                const v = String(input.value || "").trim().replace(",", ".");
                datos[campo] = v === "" ? null : parseFloat(v);
            } else if (campo === "num_pallets") {
                const v = String(input.value || "").trim();
                datos[campo] = v === "" ? null : parseInt(v, 10);
            } else if (campo === "destino") {
                const v = String(input.value || "").trim();
                datos.destino = v === "" ? null : v;
                datos.destino_auto = (input.dataset.auto || "") || null;
            } else if (campo === "observaciones") {
                const v = String(input.value || "").trim();
                datos.observaciones = v === "" ? null : v;
            } else if (campo === "transportista_nombre") {
                const v = String(input.value || "").trim();
                datos.transportista_nombre = v === "" ? null : v;
            } else if (campo === "transportista_nif") {
                const v = String(input.value || "").trim();
                datos.transportista_nif = v === "" ? null : v;
            }
        });
    datos.transportista_codigo = fila.dataset.transportista || null;
    return datos;
}

function validarFila(fila) {
    let ok = true;
    fila.querySelectorAll("input.matricula").forEach(function (input) {
        const valor = normalizar(input.value);
        input.value = valor;
        const obligatorio = input.dataset.campo === "matricula_tractor";
        if (!valor) {
            input.classList.toggle("invalido", obligatorio);
            if (obligatorio) ok = false;
            return;
        }
        input.classList.remove("invalido");
    });
    fila.querySelectorAll("input.editable-num").forEach(function (input) {
        const v = String(input.value || "").trim().replace(",", ".");
        if (v === "") {
            input.classList.remove("invalido");
            return;
        }
        const num = Number(v);
        const valido = !isNaN(num) && num >= 0;
        input.classList.toggle("invalido", !valido);
        if (!valido) ok = false;
    });
    return ok;
}

async function guardarMatriculas(fila) {
    return pedirJson("/albaranes/" + encodeURIComponent(fila.dataset.sdhnum), {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(leerMatriculas(fila))
    });
}

async function generarDeca(numalbaran) {
    const fila = cuerpoTabla.querySelector('tr[data-sdhnum="' + numalbaran + '"]');
    if (!fila) return;
    if (!validarFila(fila)) {
        mostrarMensaje("Revise las matriculas del albaran " + numalbaran, true);
        return;
    }
    try {
        await guardarMatriculas(fila);
        const resultado = await pedirJson(
            "/albaranes/" + encodeURIComponent(numalbaran) + "/deca",
            { method: "POST" }
        );
        mostrarMensaje("DeCA generado: " + resultado.url);
        await cargarAlbaranes();
        await cargarOrigenes();
    } catch (e) {
        mostrarMensaje("Error al generar el DeCA de " + numalbaran + ": " + e.message, true);
    }
}

async function generarSeleccionados() {
    const seleccionadas = Array.prototype.slice.call(
        cuerpoTabla.querySelectorAll("tr[data-sdhnum]")
    ).filter(function (fila) {
        const casilla = fila.querySelector("input.seleccion");
        return casilla && casilla.checked;
    });
    if (!seleccionadas.length) {
        mostrarMensaje("Seleccione al menos un albaran", true);
        return;
    }
    for (let i = 0; i < seleccionadas.length; i++) {
        const fila = seleccionadas[i];
        if (!validarFila(fila)) {
            mostrarMensaje("Revise las matriculas de " + fila.dataset.sdhnum, true);
            return;
        }
        try { await guardarMatriculas(fila); }
        catch (e) {
            mostrarMensaje("Error al guardar " + fila.dataset.sdhnum + ": " + e.message, true);
            return;
        }
    }

    const numeros = seleccionadas.map(function (f) { return f.dataset.sdhnum; })
        .filter(function (n, idx, arr) { return n && arr.indexOf(n) === idx; });
    if (numeros.length < 1) {
        mostrarMensaje("Seleccione al menos un albaran valido", true);
        return;
    }
    if (numeros.length === 1) {
        await generarDeca(numeros[0]);
        return;
    }

    let pesoTotal = 0, paletsTotal = 0;
    const destinos = [], vistos = {};
    seleccionadas.forEach(function (fila) {
        pesoTotal += parseFloat(fila.dataset.peso || "0") || 0;
        paletsTotal += parseInt(fila.dataset.palets || "0", 10) || 0;
        const d = (fila.dataset.destino || "").trim();
        if (d && !vistos[d]) { vistos[d] = true; destinos.push(d); }
    });

    divResumen.style.display = "block";
    divResumen.className = "resumen ok";
    divResumen.innerHTML =
        "<h3>Generacion consolidada de DeCA</h3>"
        + "<p><strong>Albaranes (" + numeros.length + "):</strong> " + esc(numeros.join(", ")) + "</p>"
        + "<p><strong>Peso total:</strong> " + pesoTotal.toFixed(2) + " kg</p>"
        + "<p><strong>Palets totales:</strong> " + paletsTotal + "</p>"
        + "<p><strong>Destinos apilados:</strong><br>" + destinos.map(esc).join("<br>") + "</p>"
        + "<p>Naturaleza: MENAJE DE PLÁSTICO</p>"
        + "<p>Generando un unico PDF consolidado...</p>";

    try {
        const resultado = await pedirJson("/albaranes/deca-multi", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ numalbaranes: numeros })
        });
        divResumen.innerHTML +=
            "<p><strong>Resultado:</strong> " + esc(resultado.mensaje) + "</p>"
            + "<p><a href=\"" + esc(resultado.url) + "\" target=\"_blank\">Descargar DeCA</a></p>";
        mostrarMensaje("DeCA consolidado generado: " + resultado.url);
        await cargarAlbaranes();
        await cargarOrigenes();
    } catch (e) {
        divResumen.className = "resumen error";
        divResumen.innerHTML += "<p><strong>Error:</strong> " + esc(e.message) + "</p>";
        mostrarMensaje("Error al generar el DeCA consolidado: " + e.message, true);
    }
}

async function enviarEmail(numalbaran) {
    const email = window.prompt("Email del conductor para el DeCA del albaran " + numalbaran + ":");
    if (!email) return;
    try {
        const resultado = await pedirJson(
            "/albaranes/" + encodeURIComponent(numalbaran) + "/deca/email",
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: email })
            }
        );
        mostrarMensaje(resultado.mensaje);
    } catch (e) {
        mostrarMensaje("Error al enviar el email: " + e.message, true);
    }
}

cuerpoTabla.addEventListener("focusout", async function (evento) {
    if (!evento.target.classList.contains("matricula")
        && !evento.target.classList.contains("editable-num")
        && !evento.target.classList.contains("editable-texto")) return;
    const fila = evento.target.closest("tr");
    if (!validarFila(fila)) return;
    try {
        await guardarMatriculas(fila);
        // Actualizar dataset para multi-consolidacion
        const datos = leerMatriculas(fila);
        if (datos.peso_kg != null) fila.dataset.peso = String(datos.peso_kg);
        if (datos.num_pallets != null) fila.dataset.palets = String(datos.num_pallets);
        const inputDestino = fila.querySelector("input.editable-texto[data-campo='destino']");
        if (inputDestino) {
            // Si se dejo vacio se vuelve al destino del albaran (automatico)
            if (!datos.destino) inputDestino.value = inputDestino.dataset.auto || "";
            fila.dataset.destino = inputDestino.value.trim();
        }
        mostrarMensaje("Datos guardados para " + fila.dataset.sdhnum);
    } catch (e) {
        mostrarMensaje("Error al guardar: " + e.message, true);
    }
});

cuerpoTabla.addEventListener("click", function (evento) {
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) return;
    const fila = boton.closest("tr");
    const numalbaran = fila.dataset.sdhnum;
    if (boton.dataset.accion === "generar") generarDeca(numalbaran);
    else if (boton.dataset.accion === "email") enviarEmail(numalbaran);
});

document.querySelectorAll("th.ordenable").forEach(function (th) {
    th.style.cursor = "pointer";
    th.title = "Ordenar por esta columna";
    th.addEventListener("click", function () {
        const campo = th.dataset.campo;
        if (ordenarPor === campo) {
            ordenDesc = !ordenDesc;
        } else {
            ordenarPor = campo;
            ordenDesc = (campo === "fecha_transporte" || campo === "peso_kg" || campo === "num_pallets");
        }
        cargarAlbaranes();
    });
});

chkTodos.addEventListener("change", function () {
    cuerpoTabla.querySelectorAll("input.seleccion").forEach(function (casilla) {
        casilla.checked = chkTodos.checked;
    });
});

// ---------------------------------------------------------------------------
// DeCA sin albaran: formulario con arrastre de datos del cargador por origen
// ---------------------------------------------------------------------------

// Origenes con detalle del cargador (se cachean para arrastrar los datos)
let origenesDetalle = [];

async function cargarOrigenesDetalle() {
    try {
        origenesDetalle = await pedirJson("/albaranes/origenes-detalle");
    } catch (e) {
        console.warn("No se pudieron cargar los origenes con detalle:", e.message);
        origenesDetalle = [];
        return;
    }
    selManOrigen.innerHTML = "<option value=\"\">Seleccione un origen...</option>";
    origenesDetalle.forEach(function (o) {
        const opt = document.createElement("option");
        opt.value = o.origen;
        opt.textContent = o.origen + " - " + (o.cargador_nombre || "");
        selManOrigen.appendChild(opt);
    });
}

function pintarCargadorManual() {
    const elegido = origenesDetalle.find(function (o) {
        return o.origen === selManOrigen.value;
    });
    if (!elegido) {
        divManCargador.innerHTML = "";
        return;
    }
    divManCargador.innerHTML =
        "<p><strong>Cargador contractual arrastrado del origen:</strong></p>"
        + "<p><strong>Nombre:</strong> " + esc(elegido.cargador_nombre || "-") + "</p>"
        + "<p><strong>NIF:</strong> " + esc(elegido.cargador_nif || "-") + "</p>"
        + "<p><strong>Domicilio:</strong> " + esc(elegido.cargador_domicilio || "-") + "</p>"
        + "<p><strong>Naturaleza de la mercancia:</strong> "
        + esc(elegido.naturaleza_mercancia || "-") + "</p>";
}

function abrirPanelManual() {
    if (!origenesDetalle.length) {
        cargarOrigenesDetalle();
    }
    if (!txtManFecha.value) {
        txtManFecha.value = new Date().toISOString().slice(0, 10);
    }
    panelManual.style.display = "block";
    selManOrigen.focus();
}

function cerrarPanelManual() {
    panelManual.style.display = "none";
}

async function generarDecaManual() {
    const origen = selManOrigen.value;
    const matTractor = normalizar(txtManMatTractor.value);
    const matRemolque = normalizar(txtManMatRemolque.value);
    txtManMatTractor.value = matTractor;
    txtManMatRemolque.value = matRemolque;

    if (!origen) {
        mostrarMensaje("Seleccione el origen del DeCA", true);
        return;
    }
    if (!matTractor) {
        txtManMatTractor.classList.add("invalido");
        mostrarMensaje("Indique la matricula del vehiculo tractor", true);
        return;
    }
    txtManMatTractor.classList.remove("invalido");
    txtManMatRemolque.classList.remove("invalido");

    const carga = {
        origen: origen,
        fecha_transporte: txtManFecha.value || null,
        destino: txtManDestino.value.trim() || null,
        transportista_nombre: txtManTransportista.value.trim() || null,
        transportista_nif: txtManTransportistaNif.value.trim() || null,
        matricula_tractor: matTractor,
        matricula_remolque: matRemolque || null,
        peso_kg: txtManPeso.value === "" ? null : parseFloat(txtManPeso.value),
        num_pallets: txtManPalets.value === "" ? null : parseInt(txtManPalets.value, 10),
        observaciones: txtManObservaciones.value.trim() || null
    };

    btnManGenerar.disabled = true;
    btnManGenerar.textContent = "Generando...";
    try {
        const resultado = await pedirJson("/albaranes/deca-manual", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(carga)
        });
        txtManReferencia.value = resultado.numalbaran;
        mostrarMensaje(resultado.mensaje + " -> " + resultado.url);
        divManCargador.innerHTML +=
            "<p><strong>Resultado:</strong> <a href=\"" + esc(resultado.url)
            + "\" target=\"_blank\">Descargar DeCA " + esc(resultado.numalbaran) + "</a></p>";
    } catch (e) {
        mostrarMensaje("Error al generar el DeCA sin albaran: " + e.message, true);
    } finally {
        btnManGenerar.disabled = false;
        btnManGenerar.textContent = "Generar DeCA";
    }
}

document.getElementById("btn-manual").addEventListener("click", abrirPanelManual);
document.getElementById("btn-manual-cancelar").addEventListener("click", cerrarPanelManual);
document.getElementById("btn-manual-generar").addEventListener("click", generarDecaManual);
selManOrigen.addEventListener("change", pintarCargadorManual);

document.getElementById("btn-refrescar").addEventListener("click", async function () {
    await cargarOrigenes();
    await cargarAlbaranes();
});
document.getElementById("btn-generar-seleccion").addEventListener("click", generarSeleccionados);
chkPendientes.addEventListener("change", async function () {
    await cargarOrigenes();
    await cargarAlbaranes();
});
numLimite.addEventListener("change", cargarAlbaranes);
selOrigen.addEventListener("change", cargarAlbaranes);
txtTransportista.addEventListener("keyup", function (evento) {
    if (evento.key === "Enter") cargarAlbaranes();
});
txtFiltro.addEventListener("keyup", function (evento) {
    if (evento.key === "Enter") cargarAlbaranes();
});

(async function inicio() {
    await cargarOrigenes();
    await cargarAlbaranes();
})();


