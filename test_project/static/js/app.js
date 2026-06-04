/**
 * Aplicación de Resumen de Pedidos Web
 * Maneja la carga y visualización de datos desde la API
 */

class PedidosApp {
    constructor() {
        this.data = [];
        this.currentPage = this.detectPage();
        this.init();
    }

    /**
     * Detectar en qué página estamos
     */
    detectPage() {
        const path = window.location.pathname;
        if (path.startsWith('/pedidos/')) return 'list';
        if (path.startsWith('/pedido/')) return 'detail';
        return 'summary';
    }

    /**
     * Inicializar la aplicación
     */
    init() {
        this.bindEvents();
        
        // Cargar datos según la página
        if (this.currentPage === 'summary') {
            this.loadData();
        } else if (this.currentPage === 'list') {
            this.loadPedidosList();
        } else if (this.currentPage === 'detail') {
            this.loadPedidoDetalle();
        }
    }

    /**
     * Vincular eventos
     */
    bindEvents() {
        document.getElementById('refresh-btn').addEventListener('click', () => this.loadData());
        document.getElementById('export-btn').addEventListener('click', () => this.exportCSV());
    }

    /**
     * Cargar datos desde la API (página principal)
     */
    async loadData() {
        this.showLoading(true);
        this.updateStatus('Cargando...', 'loading');

        try {
            const response = await fetch('/api/pedidos');
            const result = await response.json();

            if (result.success) {
                this.data = result.data;
                this.renderTable();
                this.updateStatus('Conectado', 'connected');
                this.updateLastUpdate();
                document.getElementById('export-btn').disabled = false;
                
                // Ocultar mensaje de error si existe
                this.hideError();
            } else {
                this.showError(result.message || 'Error al cargar los datos');
                this.updateStatus('Error', 'error');
            }
        } catch (error) {
            this.showError(`Error de conexión: ${error.message}`);
            this.updateStatus('Error', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    /**
     * Cargar lista de pedidos (página intermedia)
     */
    async loadPedidosList() {
        if (typeof USUARIO === 'undefined') {
            this.showError('Parámetros no definidos');
            return;
        }

        this.showLoading(true);
        this.updateStatus('Cargando...', 'loading');

        try {
            const response = await fetch(`/api/pedidos/${encodeURIComponent(USUARIO)}`);
            const result = await response.json();

            if (result.success) {
                this.data = result.data;
                this.renderPedidosList();
                this.updateStatus('Conectado', 'connected');
                this.updateLastUpdate();
                document.getElementById('export-btn').disabled = false;
                this.hideError();
            } else {
                this.showError(result.message || 'Error al cargar los datos');
                this.updateStatus('Error', 'error');
            }
        } catch (error) {
            this.showError(`Error de conexión: ${error.message}`);
            this.updateStatus('Error', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    /**
     * Cargar detalle de pedido
     */
    async loadPedidoDetalle() {
        if (typeof USUARIO === 'undefined' || typeof NUMPED === 'undefined') {
            this.showError('Parámetros no definidos');
            return;
        }

        this.showLoading(true);
        this.updateStatus('Cargando...', 'loading');

        try {
            const response = await fetch(`/api/pedido/${encodeURIComponent(USUARIO)}/${encodeURIComponent(NUMPED)}`);
            const result = await response.json();

            if (result.success) {
                this.renderPedidoDetalle(result.resumen, result.lineas);
                this.updateStatus('Conectado', 'connected');
                this.updateLastUpdate();
                document.getElementById('export-btn').disabled = false;
                document.getElementById('pedido-resumen').style.display = 'block';
                this.hideError();
            } else {
                this.showError(result.message || 'Error al cargar el detalle del pedido');
                this.updateStatus('Error', 'error');
            }
        } catch (error) {
            this.showError(`Error de conexión: ${error.message}`);
            this.updateStatus('Error', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    /**
     * Renderizar tabla de resumen (página principal)
     */
    renderTable() {
        const tbody = document.getElementById('table-body');
        const tfoot = document.getElementById('table-footer');

        if (this.data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 40px; color: var(--text-secondary);">
                        No se encontraron datos
                    </td>
                </tr>
            `;
            tfoot.innerHTML = '';
            return;
        }

        // Renderizar filas
        tbody.innerHTML = this.data.map(row => `
            <tr class="clickable-row" data-usuario="${this.escapeHtml(row.usuario)}">
                <td>${this.escapeHtml(row.usuario)}</td>
                <td>${this.escapeHtml(row.nombre)}</td>
                <td class="number">${row.num_pedidos.toLocaleString()}</td>
                <td class="number">${this.formatCurrency(row.importe_total)}</td>
                <td class="number">${this.formatCurrency(row.importe_medio)}</td>
            </tr>
        `).join('');

        // Añadir evento click a las filas
        document.querySelectorAll('.clickable-row').forEach(row => {
            row.addEventListener('click', () => {
                const usuario = row.getAttribute('data-usuario');
                window.location.href = `/pedidos/${encodeURIComponent(usuario)}`;
            });
        });

        // Calcular totales
        const totalPedidos = this.data.reduce((sum, row) => sum + row.num_pedidos, 0);
        const totalImporte = this.data.reduce((sum, row) => sum + row.importe_total, 0);
        const totalMedio = totalImporte / totalPedidos || 0;

        // Renderizar pie de tabla
        tfoot.innerHTML = `
            <tr>
                <td colspan="2" style="text-align: right;">TOTALES</td>
                <td class="number">${totalPedidos.toLocaleString()}</td>
                <td class="number">${this.formatCurrency(totalImporte)}</td>
                <td class="number">${this.formatCurrency(totalMedio)}</td>
            </tr>
        `;
    }

    /**
     * Renderizar lista de pedidos (página intermedia)
     */
    renderPedidosList() {
        const tbody = document.getElementById('table-body');
        const tfoot = document.getElementById('table-footer');

        if (this.data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; padding: 40px; color: var(--text-secondary);">
                        No se encontraron pedidos
                    </td>
                </tr>
            `;
            tfoot.innerHTML = '';
            return;
        }

        // Renderizar filas con estado coloreado
        tbody.innerHTML = this.data.map(row => `
            <tr class="clickable-row" data-usuario="${this.escapeHtml(row.usuario)}" data-numped="${this.escapeHtml(row.numped)}">
                <td><strong>${this.escapeHtml(row.numped)}</strong></td>
                <td>${this.escapeHtml(row.usuario)}</td>
                <td>${this.escapeHtml(row.nombre)}</td>
                <td>${this.renderEstado(row.estado)}</td>
                <td class="number">${row.lineas.toLocaleString()}</td>
                <td class="number">${this.formatCurrency(row.importe)}</td>
            </tr>
        `).join('');

        // Añadir evento click a las filas
        document.querySelectorAll('.clickable-row').forEach(row => {
            row.addEventListener('click', () => {
                const usuario = row.getAttribute('data-usuario');
                const numped = row.getAttribute('data-numped');
                window.location.href = `/pedido/${encodeURIComponent(usuario)}/${encodeURIComponent(numped)}`;
            });
        });

        // Calcular totales
        const totalLineas = this.data.reduce((sum, row) => sum + row.lineas, 0);
        const totalImporte = this.data.reduce((sum, row) => sum + row.importe, 0);

        // Renderizar pie de tabla
        tfoot.innerHTML = `
            <tr>
                <td colspan="4" style="text-align: right;">TOTALES</td>
                <td class="number">${totalLineas.toLocaleString()}</td>
                <td class="number">${this.formatCurrency(totalImporte)}</td>
            </tr>
        `;
    }

    /**
     * Renderizar detalle de pedido
     */
    renderPedidoDetalle(resumen, lineas) {
        // Actualizar resumen
        document.getElementById('resumen-usuario').textContent = resumen.usuario;
        document.getElementById('resumen-nombre').textContent = resumen.nombre;
        document.getElementById('resumen-estado').textContent = resumen.estado;
        document.getElementById('resumen-fecha').textContent = resumen.fecha_pedido;
        document.getElementById('resumen-lineas').textContent = resumen.total_lineas;
        document.getElementById('resumen-importe').textContent = this.formatCurrency(resumen.importe_total);

        // Renderizar líneas
        const tbody = document.getElementById('table-body');
        const tfoot = document.getElementById('table-footer');

        if (lineas.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px; color: var(--text-secondary);">
                        No hay líneas en este pedido
                    </td>
                </tr>
            `;
            tfoot.innerHTML = '';
            return;
        }

        // Renderizar filas con numeración
        tbody.innerHTML = lineas.map((linea, index) => `
            <tr>
                <td class="number">${index + 1}</td>
                <td>${this.escapeHtml(linea.codart || '-')}</td>
                <td class="number">${linea.cantidad ? linea.cantidad.toLocaleString('es-ES', {minimumFractionDigits: 0}) : '0'}</td>
                <td class="number">${this.formatCurrency(linea.precio || 0)}</td>
                <td class="number">${linea.descuento1 !== undefined ? linea.descuento1.toFixed(0) + '%' : '-'}</td>
                <td class="number">${linea.descuento2 !== undefined ? linea.descuento2.toFixed(0) + '%' : '-'}</td>
                <td class="number">${this.formatCurrency(linea.importe_linea || 0)}</td>
            </tr>
        `).join('');

        // Calcular totales
        const totalCantidad = lineas.reduce((sum, l) => sum + (l.cantidad || 0), 0);
        const totalImporte = lineas.reduce((sum, l) => sum + (l.importe_linea || 0), 0);

        // Renderizar pie de tabla
        tfoot.innerHTML = `
            <tr>
                <td colspan="2" style="text-align: right;">TOTALES</td>
                <td class="number">${totalCantidad.toLocaleString('es-ES', {minimumFractionDigits: 0})}</td>
                <td></td>
                <td></td>
                <td></td>
                <td class="number">${this.formatCurrency(totalImporte)}</td>
            </tr>
        `;
    }

    /**
     * Renderizar estado con clase CSS para colores
     */
    renderEstado(estado) {
        if (!estado) return '<span class="estado-sin-estado">Sin Estado</span>';
        
        const estadoUpper = estado.toUpperCase().trim();
        let className = 'estado-otro';

        // Definir colores según el estado (exact match)
        if (estadoUpper === 'PENDIENTE') {
            className = 'estado-pendiente';
        } else if (estadoUpper === 'PROCESADO') {
            className = 'estado-procesado';
        } else if (estadoUpper === 'BORRADOR') {
            className = 'estado-borrador';
        } else if (estadoUpper === 'RECHAZADO') {
            className = 'estado-rechazado';
        } else if (estadoUpper === 'ENVIADO') {
            className = 'estado-enviado';
        } else if (estadoUpper === 'EN.PARCIAL' || estadoUpper === 'EN PARCIAL') {
            className = 'estado-parcial';
        }

        return `<span class="${className}">${this.escapeHtml(estado)}</span>`;
    }

    /**
     * Formatear moneda
     */
    formatCurrency(value) {
        return new Intl.NumberFormat('es-ES', {
            style: 'currency',
            currency: 'EUR',
            minimumFractionDigits: 2
        }).format(value);
    }

    /**
     * Escapar HTML para prevenir XSS
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Mostrar/ocultar loading
     */
    showLoading(show) {
        const loading = document.getElementById('loading');
        loading.style.display = show ? 'flex' : 'none';
    }

    /**
     * Mostrar mensaje de error
     */
    showError(message) {
        const errorDiv = document.getElementById('error-message');
        const errorText = document.getElementById('error-text');
        errorText.textContent = message;
        errorDiv.style.display = 'flex';
    }

    /**
     * Ocultar mensaje de error
     */
    hideError() {
        const errorDiv = document.getElementById('error-message');
        errorDiv.style.display = 'none';
    }

    /**
     * Actualizar estado de conexión
     */
    updateStatus(text, status) {
        const statusText = document.getElementById('status-text');
        const statusDot = document.getElementById('connection-status');
        
        statusText.textContent = text;
        statusDot.className = 'status-dot';
        
        if (status === 'connected') {
            statusDot.classList.add('connected');
        } else if (status === 'error') {
            statusDot.classList.add('error');
        }
    }

    /**
     * Actualizar fecha de última carga
     */
    updateLastUpdate() {
        const now = new Date();
        const formatted = now.toLocaleString('es-ES', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        document.getElementById('last-update').textContent = formatted;
    }

    /**
     * Exportar datos a CSV
     */
    exportCSV() {
        if (this.data.length === 0) {
            alert('No hay datos para exportar');
            return;
        }

        let csvContent = '\uFEFF'; // BOM para Excel
        const now = new Date();
        const timestamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
        let filename = `export_${timestamp}.csv`;

        if (this.currentPage === 'summary') {
            csvContent += 'Usuario;Nombre;Num_Pedidos;Importe_Total;Importe_Medio\n';
            this.data.forEach(row => {
                csvContent += `${row.usuario};${row.nombre};${row.num_pedidos};${row.importe_total.toFixed(2)};${row.importe_medio.toFixed(2)}\n`;
            });
            filename = `resumen_pedidos_${timestamp}.csv`;
        } else if (this.currentPage === 'list') {
            csvContent += 'NPedido;Usuario;Nombre;Estado;Lineas;Importe\n';
            this.data.forEach(row => {
                csvContent += `${row.numped};${row.usuario};${row.nombre};${row.estado};${row.lineas};${row.importe.toFixed(2)}\n`;
            });
            filename = `pedidos_${USUARIO}_${timestamp}.csv`;
        } else if (this.currentPage === 'detail') {
            // Para detalle, exportar con formato especial
            csvContent += `Pedido: ${NUMPED};Usuario: ${USUARIO}\n\n`;
            csvContent += 'Linea;Articulo;Cantidad;Precio_Unitario;Descuento1;Descuento2;Importe\n';
            this.data.forEach((linea, index) => {
                const cantidad = linea.cantidad ? linea.cantidad.toFixed(0) : '0';
                const precio = linea.precio ? linea.precio.toFixed(2) : '0.00';
                const descuento1 = linea.descuento1 !== undefined ? linea.descuento1.toFixed(0) : '0';
                const descuento2 = linea.descuento2 !== undefined ? linea.descuento2.toFixed(0) : '0';
                const importe = linea.importe_linea ? linea.importe_linea.toFixed(2) : '0.00';
                csvContent += `${index + 1};${linea.codart || '-'};${cantidad};${precio};${descuento1};${descuento2};${importe}\n`;
            });
            filename = `pedido_${NUMPED}_${timestamp}.csv`;
        }

        // Crear blob y descargar
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}

// Iniciar aplicación cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    const app = new PedidosApp();
});