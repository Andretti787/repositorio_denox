document.addEventListener('DOMContentLoaded', function () {

    // --- Elementos del DOM ---
    const albaranesTbody = document.getElementById('albaranes-tbody');
    const albaranesLoading = document.getElementById('albaranes-loading');
    const albaranesError = document.getElementById('albaranes-error');
    const albaranesTableContainer = document.getElementById('albaranes-table-container');
    const albaranSearchInput = document.getElementById('albaran-search-input');

    const postForm = document.getElementById('post-form');
    const apiResponseCode = document.querySelector('#api-response code');
    const apiResponseLoading = document.getElementById('api-response-loading');

    const dateFromInput = document.getElementById('date-from');
    const dateToInput = document.getElementById('date-to');
    
    let albaranesData = [];

    /**
     * Renderiza la tabla de albaranes con los datos proporcionados.
     * @param {Array} albaranes - La lista de albaranes a mostrar.
     */
    function renderAlbaranes(albaranes) {
        albaranesTbody.innerHTML = ''; // Limpiar tabla
        if (albaranes.length === 0) {
            albaranesTbody.innerHTML = '<tr><td colspan="6" class="text-center">No se encontraron albaranes que coincidan con la búsqueda.</td></tr>';
        } else {
            albaranes.forEach((alb) => {
                // Encontrar el índice original para el valor del radio button
                const originalIndex = albaranesData.findIndex(originalAlb => originalAlb.NUMALB === alb.NUMALB);

                // La fecha ya viene en formato YYYY-MM-DD desde el backend, no necesita conversión.
                const formattedDate = alb.FECHA_ENVIO;
                const peso = parseFloat(alb.PESO).toFixed(2);

                const row = `<tr>
                    <td><input type="radio" name="albaran-select" value="${originalIndex}"></td>
                    <td>${alb.NUMALB}</td>
                    <td>${formattedDate}</td>
                    <td>${alb.PALLETS}</td>
                    <td>${alb.BULTOS}</td>
                    <td>${peso}</td>
                </tr>`;
                albaranesTbody.insertAdjacentHTML('beforeend', row);
            });
        }
    }
    /**
     * Función para cargar los albaranes desde nuestro backend Flask.
     */
    async function cargarAlbaranes() {
        try {
            const response = await fetch('/albaranes');
            const responseData = await response.json();

            if (!response.ok) {
                // Usar el mensaje de error del backend si está disponible
                const errorMessage = responseData.error || `Error HTTP: ${response.status}`;
                const errorDetails = responseData.details || 'No hay más detalles.';
                throw new Error(`${errorMessage} - ${errorDetails}`);
            }
            albaranesData = responseData;

            renderAlbaranes(albaranesData); // Renderizar la tabla inicial
            albaranesTableContainer.classList.remove('d-none');
        } catch (error) {
            console.error('Error al cargar albaranes:', error);
            albaranesError.textContent = `Error al cargar albaranes: ${error.message}`;
            albaranesError.classList.remove('d-none'); // Mostrar el div de error
        } finally {
            albaranesLoading.classList.add('d-none');
        }
    }

    /**
     * Muestra el estado de carga en el área de respuesta de la API.
     * @param {boolean} isLoading - Si se debe mostrar el spinner de carga.
     */
    function setApiResponseLoading(isLoading) {
        if (isLoading) {
            apiResponseLoading.classList.remove('d-none');
            apiResponseCode.parentElement.classList.add('d-none');
        } else {
            apiResponseLoading.classList.add('d-none');
            apiResponseCode.parentElement.classList.remove('d-none');
        }
    }

    /**
     * Muestra la respuesta de la API en el área designada.
     * @param {object} data - El objeto JSON de respuesta.
     * @param {number} status - El código de estado HTTP de la respuesta.
     */
    function mostrarApiResponse(data, status) {
        const statusClass = status >= 200 && status < 300 ? 'text-success' : 'text-danger';
        apiResponseCode.innerHTML = `<strong>Estado: <span class="${statusClass}">${status}</span></strong>\n\n`;
        apiResponseCode.innerHTML += JSON.stringify(data, null, 2);
    }

    // --- Event Listeners ---

    const listForm = document.getElementById('list-form');
    const listTbody = document.getElementById('list-tbody');
    const listLoading = document.getElementById('list-loading');
    const listError = document.getElementById('list-error');
    const listTable = document.getElementById('list-table');

    listForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        listLoading.classList.remove('d-none');
        listError.classList.add('d-none');
        listTable.classList.add('d-none');
        listTbody.innerHTML = '';

        const dateFrom = document.getElementById('date-from').value;
        const dateTo = document.getElementById('date-to').value;

        try {
            const response = await fetch(`/listar-ordenes?date_from=${dateFrom}&date_to=${dateTo}`);
            const data = await response.json();

            if (!response.ok) {
                const errorData = data.details || data.error || 'Error desconocido del servidor';
                throw new Error(typeof errorData === 'object' ? JSON.stringify(errorData) : errorData);
            }

            if (data.length === 0) {
                listTbody.innerHTML = '<tr><td colspan="6" class="text-center">No se encontraron órdenes para el rango de fechas seleccionado.</td></tr>';
            } else {
                data.forEach(order => {
                    // Buscar el número de albarán en las referencias
                    const albaranRef = order.references ? order.references.find(ref => ref.code === '007') : undefined;
                    const numAlbaran = albaranRef ? albaranRef.value : 'N/A';

                    const row = `<tr>
                        <td>${order.id}</td>
                        <td>${numAlbaran}</td>
                        <td>${order.transportDate}</td>
                        <td>${order.state}</td>
                        <td>${order.consignee.names.join(', ')}</td>
                        <td>
                            <button class="btn btn-sm btn-secondary print-label-btn" data-order-id="${order.id}">Imprimir</button>
                            <button class="btn btn-sm btn-danger delete-order-btn" data-order-id="${order.id}">Eliminar</button>
                        </td>
                    </tr>`;
                    listTbody.insertAdjacentHTML('beforeend', row);
                });
            }
            listTable.classList.remove('d-none');
        } catch (error) {
            console.error('Error al listar órdenes:', error);
            listError.textContent = error.message;
            listError.classList.remove('d-none');
        } finally {
            listLoading.classList.add('d-none');
        }
    });

    // Listener para los botones de la tabla de órdenes (imprimir y eliminar)
    listTbody.addEventListener('click', async (e) => {
        const target = e.target;

        // Botón de Imprimir
        if (target.classList.contains('print-label-btn')) {
            const orderId = target.dataset.orderId;
            if (!orderId) return;

            // Llamar directamente a la función que descarga la etiqueta
            descargarEtiqueta(orderId);
            // Cambiar a la primera pestaña para ver el resultado
            new bootstrap.Tab(document.getElementById('post-tab')).show();
        }

        // Botón de Eliminar
        if (target.classList.contains('delete-order-btn')) {
            const orderId = target.dataset.orderId;
            if (!orderId) return;

            if (confirm(`¿Estás seguro de que quieres eliminar la orden ${orderId}? Esta acción no se puede deshacer.`)) {
                try {
                    const response = await fetch(`/eliminar-orden/${orderId}`, {
                        method: 'DELETE'
                    });
                    const data = await response.json();

                    if (response.ok) {
                        // Eliminar la fila de la tabla
                        target.closest('tr').remove();
                        // Mostrar notificación de éxito
                        mostrarApiResponse(data, response.status);
                    } else {
                        throw new Error(data.details || data.error || 'Error al eliminar');
                    }
                } catch (error) {
                    console.error('Error al eliminar la orden:', error);
                    mostrarApiResponse({ error: 'Fallo al contactar el backend para eliminar la orden.', details: error.message }, 500);
                }
            }
        }
    });

    albaranesTbody.addEventListener('change', function(e) {
        if (e.target.name === 'albaran-select') {
            const selectedIndex = e.target.value;
            const selectedAlbaran = albaranesData[selectedIndex];

            if (selectedAlbaran) {
                const postDataEl = document.getElementById('post-data');
                // Rellenar el textarea con el albarán seleccionado
                // CLAVE: Convertimos el objeto a un objeto plano antes de stringify.
                // El objeto que viene del fetch puede tener tipos de datos complejos (como fechas)
                // que no son directamente serializables. Al hacer esto, nos aseguramos de que es un objeto JS simple.
                const plainAlbaranObject = { ...selectedAlbaran };
                postDataEl.value = JSON.stringify(plainAlbaranObject, null, 2);
            }
        }
    });

    // Listener para el campo de búsqueda de albaranes
    albaranSearchInput.addEventListener('input', function(e) {
        const searchTerm = e.target.value.toLowerCase();

        if (!searchTerm) {
            renderAlbaranes(albaranesData); // Si no hay búsqueda, mostrar todos
            return;
        }

        const filteredAlbaranes = albaranesData.filter(alb => 
            alb.NUMALB.toLowerCase().includes(searchTerm) || alb.NOMBRE_CONSIGNATARIO.toLowerCase().includes(searchTerm)
        );
        renderAlbaranes(filteredAlbaranes);
    });
    /*
    // Listener para el formulario de obtención de etiquetas
    // COMENTADO: Este bloque causaba un error porque el elemento con id 'get-form' no existe en index.html,
    // lo que detenía la ejecución del script antes de que se pudiera llamar a cargarAlbaranes().
    // La funcionalidad de imprimir etiquetas ahora se maneja desde la tabla "Listar Órdenes".
    const getForm = document.getElementById('get-form');
    if (getForm) { // Añadir una comprobación para evitar errores si el elemento no existe
        getForm.addEventListener('submit', async (e) => { ... });
    }
    */
    /**
     * Llama al backend para descargar la etiqueta de una orden específica.
     * @param {string} orderId - El ID de la orden de transporte.
     */
    async function descargarEtiqueta(orderId) {
        setApiResponseLoading(true);
        try {
            const response = await fetch(`/llamar-api-etiquetas/${orderId}`, {
                method: 'POST'
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = `etiqueta_${orderId}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);

                mostrarApiResponse({ status: "OK", mensaje: `La descarga de la etiqueta para la orden ${orderId} ha comenzado.` }, response.status);
            } else {
                const data = await response.json();
                mostrarApiResponse(data, response.status);
            }
        } catch (error) {
            console.error('Error al solicitar etiquetas:', error);
            mostrarApiResponse({ error: 'Fallo la conexión con el backend' }, 500);
        } finally {
            setApiResponseLoading(false);
        }
    }

    // Listener para el formulario POST
    postForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        setApiResponseLoading(true);
        const postDataEl = document.getElementById('post-data');
        let body;

        try {
            body = JSON.parse(postDataEl.value);
        } catch (error) {
            mostrarApiResponse({ error: 'El JSON en el área de texto no es válido.' }, 400);
            setApiResponseLoading(false);
            return;
        }

        try {
            const response = await fetch('/crear-orden', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(body)
            });
            const data = await response.json();
            mostrarApiResponse(data, response.status);

            // Si la orden se crea con éxito, rellenamos el campo del GET y disparamos la descarga
            if (response.ok && data.id) {
                // Descargar la etiqueta automáticamente
                descargarEtiqueta(data.id);
            }

        } catch (error) {
            console.error('Error en la llamada POST:', error);
            mostrarApiResponse({ error: 'Fallo la conexión con el backend' }, 500);
        } finally {
            setApiResponseLoading(false);
        }
    });


    // --- Carga inicial ---
    /**
     * Establece la fecha de hoy en los campos de fecha para listar órdenes.
     */
    function setTodayDate() {
        const today = new Date().toISOString().slice(0, 10);
        dateFromInput.value = today;
        dateToInput.value = today;
    }

    setTodayDate();
    cargarAlbaranes();
});
