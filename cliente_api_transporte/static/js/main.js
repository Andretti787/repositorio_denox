const AppTransporte = {
    // Propiedades para almacenar elementos del DOM y datos
    albaranesTbody: null,
    albaranesLoading: null,
    albaranesError: null,
    albaranesTableContainer: null,
    albaranSearchInput: null,
    selectAllCheckbox: null,
    postForm: null,
    apiResponseCode: null,
    apiResponseLoading: null,
    dateFromInput: null,
    dateToInput: null,
    listForm: null,
    listTbody: null,
    listLoading: null,
    listError: null,
    listTable: null,
    
    albaranesData: [],

    /**
     * Renderiza la tabla de albaranes con los datos proporcionados.
     * @param {Array} albaranes - La lista de albaranes a mostrar.
     */
    renderAlbaranes(albaranes) {
        this.albaranesTbody.innerHTML = ''; // Limpiar tabla
        if (albaranes.length === 0) {
            this.albaranesTbody.innerHTML = '<tr><td colspan="7" class="text-center">No se encontraron albaranes que coincidan con la búsqueda.</td></tr>';
        } else {
            albaranes.forEach((alb) => {
                // Encontrar el índice original para el valor del radio button
                const originalIndex = this.albaranesData.findIndex(originalAlb => originalAlb.NUMALB === alb.NUMALB);

                // La fecha ya viene en formato YYYY-MM-DD desde el backend, no necesita conversión.
                const formattedDate = alb.FECHA_ENVIO;
                const peso = parseFloat(alb.PESO).toFixed(2);

                const isEnviado = alb.ENVIADO;
                const trClass = isEnviado ? 'table-warning' : ''; // Usar un color más visible
                const enviadoIcon = isEnviado ? ' <i class="fas fa-truck text-success" title="Ya enviado"></i>' : '';

                const row = `<tr data-albaran-index="${originalIndex}" class="${trClass}">
                    <td><input type="checkbox" class="albaran-select-checkbox" value="${originalIndex}"></td>
                    <td>${alb.NUMALB}${enviadoIcon}</td>
                    <td>${formattedDate}</td>
                    <td>${alb.PALLETS}</td>
                    <td>${alb.BULTOS}</td>
                    <td>${peso}</td>
                    <td><input type="text" class="form-control form-control-sm observation-input" value="${alb.OBSERVACIONES || ''}" data-index="${originalIndex}"></td>
                </tr>`;
                this.albaranesTbody.insertAdjacentHTML('beforeend', row);
            });
        }
    },
    /**
     * Función para cargar los albaranes desde nuestro backend Flask.
     */
    async cargarAlbaranes() {
        try {
            const response = await fetch('/albaranes');
            const responseData = await response.json();

            if (!response.ok) {
                // Usar el mensaje de error del backend si está disponible
                const errorMessage = responseData.error || `Error HTTP: ${response.status}`;
                const errorDetails = responseData.details || 'No hay más detalles.';
                throw new Error(`${errorMessage} - ${errorDetails}`);
            }
            this.albaranesData = responseData.map(alb => ({ ...alb, OBSERVACIONES: alb.OBSERVACIONES || '' })); // Inicializar OBSERVACIONES

            this.renderAlbaranes(this.albaranesData); // Renderizar la tabla inicial
            this.albaranesTableContainer.classList.remove('d-none');
        } catch (error) {
            console.error('Error al cargar albaranes:', error);
            this.albaranesError.textContent = `Error al cargar albaranes: ${error.message}`;
            this.albaranesError.classList.remove('d-none'); // Mostrar el div de error
        } finally {
            this.albaranesLoading.classList.add('d-none');
        }
    },

    /**
     * Muestra el estado de carga en el área de respuesta de la API.
     * @param {boolean} isLoading - Si se debe mostrar el spinner de carga.
     */
    setApiResponseLoading(isLoading) {
        if (isLoading) {
            this.apiResponseLoading.classList.remove('d-none');
            this.apiResponseCode.parentElement.classList.add('d-none');
        } else {
            this.apiResponseLoading.classList.add('d-none');
            this.apiResponseCode.parentElement.classList.remove('d-none');
        }
    },

    /**
     * Muestra la respuesta de la API en el área designada.
     * @param {object} data - El objeto JSON de respuesta.
     * @param {number} status - El código de estado HTTP de la respuesta.
     */
    mostrarApiResponse(data, status) {
        const statusClass = status >= 200 && status < 300 ? 'text-success' : 'text-danger';
        this.apiResponseCode.innerHTML = `<strong>Estado: <span class="${statusClass}">${status}</span></strong>\n\n`;
        this.apiResponseCode.innerHTML += JSON.stringify(data, null, 2);
    },

    /**
     * Descarga un fichero PDF a partir de su contenido en Base64.
     * @param {string} base64Data - El contenido del fichero en Base64.
     * @param {string} nombreArchivo - El nombre que tendrá el fichero descargado.
     */
    descargarPdfDesdeBase64(base64Data, nombreArchivo) {
        try {
            const byteCharacters = atob(base64Data);
            const byteNumbers = new Array(byteCharacters.length);
            for (let i = 0; i < byteCharacters.length; i++) {
                byteNumbers[i] = byteCharacters.charCodeAt(i);
            }
            const byteArray = new Uint8Array(byteNumbers);
            const blob = new Blob([byteArray], { type: 'application/pdf' });

            const fileURL = URL.createObjectURL(blob);
            
            // Abrir el PDF en una nueva pestaña del navegador
            const newWindow = window.open(fileURL, '_blank');
            if (!newWindow) {
                alert('No se pudo abrir la nueva pestaña. Por favor, deshabilita el bloqueador de ventanas emergentes para este sitio.');
            }
        } catch (error) {
            console.error("Error al decodificar o abrir el PDF:", error);
            alert("Hubo un error al procesar el fichero PDF para mostrarlo.");
        }
    },


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
    async descargarEtiqueta(orderId) {
        this.setApiResponseLoading(true);
        try {
            const response = await fetch(`/llamar-api-etiquetas/${orderId}`, {
                method: 'POST'
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                
                // Abrir el PDF en una nueva ventana/pestaña y activar el diálogo de impresión
                const printWindow = window.open(url, '_blank');
                if (printWindow) {
                    printWindow.onload = () => {
                        // Pequeño retraso para asegurar que el PDF se ha renderizado antes de imprimir
                        setTimeout(() => {
                            printWindow.print();
                        }, 500); 
                    };
                    this.mostrarApiResponse({ status: "OK", mensaje: `La etiqueta para la orden ${orderId} se ha abierto para imprimir.` }, response.status);
                } else {
                    this.mostrarApiResponse({ error: 'No se pudo abrir la ventana de impresión. Asegúrate de que los bloqueadores de pop-ups estén desactivados.' }, 500);
                }
                window.URL.revokeObjectURL(url); // Liberar el objeto URL
            } else {
                const data = await response.json();
                this.mostrarApiResponse(data, response.status);
            }
        } catch (error) {
            console.error('Error al solicitar etiquetas:', error);
            this.mostrarApiResponse({ error: 'Fallo la conexión con el backend' }, 500);
        } finally {
            this.setApiResponseLoading(false);
        }
    },

    /**
     * Actualiza el textarea para mostrar el número de albaranes seleccionados.
     */
    actualizarTextareaSeleccion() {
        const postDataEl = document.getElementById('post-data');
        const checkboxesSeleccionados = this.albaranesTbody.querySelectorAll('.albaran-select-checkbox:checked');
        
        if (checkboxesSeleccionados.length === 1) {
            // Si solo hay uno seleccionado, mostrar su JSON y hacerlo editable.
            const selectedIndex = checkboxesSeleccionados[0].value;
            const selectedAlbaran = this.albaranesData[selectedIndex];
            postDataEl.value = JSON.stringify(selectedAlbaran, null, 2);
            postDataEl.readOnly = false;

        } else if (checkboxesSeleccionados.length > 1) {
            // Si hay varios, mostrar un mensaje y bloquear la edición.
            postDataEl.value = `${checkboxesSeleccionados.length} albaranes seleccionados. La edición no está disponible en modo múltiple.`;
            postDataEl.readOnly = true;

        } else {
            // Si no hay ninguno, mostrar el mensaje por defecto.
            postDataEl.value = 'Seleccione uno o más albaranes de la lista.';
            postDataEl.readOnly = true;

        }
    },

    /**
     * Establece la fecha de hoy en los campos de fecha para listar órdenes.
     */
    setTodayDate() {
        const today = new Date().toISOString().slice(0, 10);
        this.dateFromInput.value = today;
        this.dateToInput.value = today;
    },

    // Método de inicialización
    init() {
        // Cachear elementos del DOM
        this.albaranesTbody = document.getElementById('albaranes-tbody');
        this.albaranesLoading = document.getElementById('albaranes-loading');
        this.albaranesError = document.getElementById('albaranes-error');
        this.albaranesTableContainer = document.getElementById('albaranes-table-container');
        this.albaranSearchInput = document.getElementById('albaran-search-input');
        this.selectAllCheckbox = document.getElementById('select-all-albaranes');
        this.postForm = document.getElementById('post-form');
        this.apiResponseCode = document.querySelector('#api-response code');
        this.apiResponseLoading = document.getElementById('api-response-loading');
        this.dateFromInput = document.getElementById('date-from');
        this.dateToInput = document.getElementById('date-to');
        this.listForm = document.getElementById('list-form');
        this.listTbody = document.getElementById('list-tbody');
        this.listLoading = document.getElementById('list-loading');
        this.listError = document.getElementById('list-error');
        this.listTable = document.getElementById('list-table');

        // Registrar Event Listeners
        this.albaranSearchInput.addEventListener('input', (e) => {
            const searchTerm = e.target.value.toLowerCase();
            const filteredAlbaranes = this.albaranesData.filter(alb => 
                alb.NUMALB.toLowerCase().includes(searchTerm) || alb.NOMBRE_CONSIGNATARIO.toLowerCase().includes(searchTerm)
            );
            this.renderAlbaranes(filteredAlbaranes);
        });

        this.selectAllCheckbox.addEventListener('change', (e) => {
            const checkboxes = this.albaranesTbody.querySelectorAll('.albaran-select-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = e.target.checked;
            });
            this.actualizarTextareaSeleccion();
        });

        this.albaranesTbody.addEventListener('change', (e) => {
            if (e.target.classList.contains('albaran-select-checkbox')) {
                this.actualizarTextareaSeleccion();
            }
        });

        this.albaranesTbody.addEventListener('input', (e) => {
            if (e.target.classList.contains('observation-input')) {
                this.albaranesData[e.target.dataset.index].OBSERVACIONES = e.target.value;
            }
        });

        this.postForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            this.setApiResponseLoading(true);
            
            const checkboxesSeleccionados = this.albaranesTbody.querySelectorAll('.albaran-select-checkbox:checked');
            
            if (checkboxesSeleccionados.length === 0) {
                this.mostrarApiResponse({ error: 'No hay albaranes seleccionados.' }, 400);
                this.setApiResponseLoading(false);
                return;
            }
    
            let albaranesParaEnviar = [];
            let endpoint = '/crear-ordenes-multiples';
    
            if (checkboxesSeleccionados.length === 1) {
                const postDataEl = document.getElementById('post-data');
                try {
                    albaranesParaEnviar.push(JSON.parse(postDataEl.value));
                } catch (error) {
                    this.mostrarApiResponse({ error: 'El JSON en el área de texto no es válido.' }, 400);
                    this.setApiResponseLoading(false);
                    return;
                }
            } else {
                checkboxesSeleccionados.forEach(checkbox => {
                    albaranesParaEnviar.push(this.albaranesData[checkbox.value]);
                });
            }
    
            const hayEnviados = albaranesParaEnviar.some(alb => alb.ENVIADO);
            let forzarReenvio = false;
    
            if (hayEnviados) {
                if (confirm("Uno o más de los albaranes seleccionados ya han sido enviados. ¿Deseas reenviarlos de todas formas?")) {
                    forzarReenvio = true;
                } else {
                    this.setApiResponseLoading(false);
                    this.mostrarApiResponse({ info: 'Operación cancelada por el usuario.' }, 400);
                    return;
                }
            }
    
            try {
                const basketType = document.querySelector('input[name="creationType"]:checked').value;
                const params = new URLSearchParams();
                if (forzarReenvio) params.append('force', 'true');
                params.append('basket', basketType);

                const response = await fetch(`${endpoint}?${params.toString()}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(albaranesParaEnviar)
                });
                const data = await response.json();
                
                // Se muestra la respuesta JSON completa en el área de depuración.
                this.mostrarApiResponse(data, response.status);
    
                // NUEVA LÓGICA: Si la respuesta es exitosa y contiene un PDF en base64, lo descargamos.
                if (response.ok && data.status === 'success' && data.type === 'pdf_base64' && data.data) {
                    const esMultiple = albaranesParaEnviar.length > 1;
                    const nombreArchivo = esMultiple ? 'etiquetas_combinadas.pdf' : `etiqueta_${data.details?.id || 'orden'}.pdf`;
                    
                    this.descargarPdfDesdeBase64(data.data, nombreArchivo);
                    
                    // Recargamos los albaranes para que se muestren como "Enviados".
                    this.cargarAlbaranes();
                } else if (!response.ok) {
                    // Si la respuesta no fue 'ok', no hacemos nada más, el error ya se muestra en `mostrarApiResponse`.
                    console.error("La creación de la orden falló. Respuesta del servidor:", data);
                }
            } catch (error) {
                console.error('Error en la llamada POST:', error);
                this.mostrarApiResponse({ error: 'Fallo la conexión con el backend' }, 500);
            } finally {
                this.setApiResponseLoading(false);
            }
        });

        this.listForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            this.listLoading.classList.remove('d-none');
            this.listError.classList.add('d-none');
            this.listTable.classList.add('d-none');
            this.listTbody.innerHTML = '';
    
            const dateFrom = this.dateFromInput.value;
            const dateTo = this.dateToInput.value;
    
            try {
                const response = await fetch(`/listar-ordenes?date_from=${dateFrom}&date_to=${dateTo}`);
                const data = await response.json();
    
                if (!response.ok) {
                    const errorData = data.details || data.error || 'Error desconocido del servidor';
                    throw new Error(typeof errorData === 'object' ? JSON.stringify(errorData) : errorData);
                }
    
                if (data.length === 0) {
                    this.listTbody.innerHTML = '<tr><td colspan="6" class="text-center">No se encontraron órdenes para el rango de fechas seleccionado.</td></tr>';
                } else {
                    data.forEach(order => {
                        const albaranRef = order.references ? order.references.find(ref => ref.code === '007') : { value: 'N/A' };
                        const isSent = order.state && order.state.toLowerCase() === 'sent';
                        const deleteButtonDisabled = isSent ? 'disabled' : '';
                        const deleteButtonTitle = isSent ? 'No se puede eliminar una orden ya enviada' : 'Eliminar orden';
                        const row = `<tr>
                            <td>${order.id}</td>
                            <td>${albaranRef.value}</td>
                            <td>${order.transportDate}</td>
                            <td>${order.state}</td>
                            <td>${order.consignee.names.join(', ')}</td>
                            <td>
                                <button class="btn btn-sm btn-secondary print-label-btn" data-order-id="${order.id}">Imprimir</button>
                                <button class="btn btn-sm btn-danger delete-order-btn" data-order-id="${order.id}" ${deleteButtonDisabled} title="${deleteButtonTitle}">Eliminar</button>
                            </td>
                        </tr>`;
                        this.listTbody.insertAdjacentHTML('beforeend', row);
                    });
                }
                this.listTable.classList.remove('d-none');
            } catch (error) {
                console.error('Error al listar órdenes:', error);
                this.listError.textContent = error.message;
                this.listError.classList.remove('d-none');
            } finally {
                this.listLoading.classList.add('d-none');
            }
        });

        this.listTbody.addEventListener('click', async (e) => {
            const target = e.target;
            const orderId = target.dataset.orderId;
            if (!orderId) return;

            if (target.classList.contains('print-label-btn')) {
                this.descargarEtiqueta(orderId);
                new bootstrap.Tab(document.getElementById('post-tab')).show();
            }

            if (target.classList.contains('delete-order-btn')) {
                if (confirm(`¿Estás seguro de que quieres eliminar la orden ${orderId}?`)) {
                    try {
                        const response = await fetch(`/eliminar-orden/${orderId}`, { method: 'DELETE' });
                        const data = await response.json();
    
                        if (response.ok) {
                            target.closest('tr').remove();
                            this.mostrarApiResponse(data, response.status);
                        } else {
                            throw new Error(data.details || data.error || 'Error al eliminar');
                        }
                    } catch (error) {
                        console.error('Error al eliminar la orden:', error);
                        this.mostrarApiResponse({ error: 'Fallo al contactar el backend para eliminar la orden.', details: error.message }, 500);
                    }
                }
            }
        });

        // Carga inicial
        this.setTodayDate();
        this.cargarAlbaranes();
    }
};

document.addEventListener('DOMContentLoaded', () => {
    AppTransporte.init();
});
