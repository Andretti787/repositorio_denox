# Manual de Usuario - Interfaz de Transporte (La Tacita 2.0)

## 1. Introducción

Este manual describe el funcionamiento de la aplicación "La Tacita 2.0", una interfaz diseñada para gestionar la creación y seguimiento de órdenes de transporte con el proveedor Dachser, utilizando los albaranes de nuestro sistema.

## 2. Pantalla Principal

La aplicación se divide en dos secciones principales:

1.  **Mis Albaranes (para Dascher):** Un listado de los albaranes disponibles para generar envíos.
2.  **API Proveedor de Transporte:** Herramientas para interactuar con la API de Dachser (crear, listar, imprimir y eliminar órdenes).

 <!-- Nota: Reemplazar con una captura de pantalla real -->

---

## 3. Crear una Orden de Transporte

Este es el flujo de trabajo principal para enviar un albarán al transportista.

### 3.1. Seleccionar un Albarán

1.  La tabla de la izquierda, **"Mis Albaranes"**, muestra los últimos 100 albaranes listos para ser enviados.
2.  Utiliza el campo de **búsqueda** para filtrar la lista por "Nº Albarán" o por "Consignatario".
3.  Una vez localizado el albarán deseado, haz clic en el **círculo de selección (radio button)** a la izquierda de la fila.

Al seleccionar un albarán, sus datos se cargarán automáticamente en formato JSON en el área de texto "Datos del Albarán para la Orden" en la sección derecha.

### 3.2. Generar la Orden

1.  Asegúrate de estar en la pestaña **"Crear Orden (POST)"**.
2.  Verifica que los datos en el área de texto son correctos. No es necesario modificarlos manualmente.
3.  Haz clic en el botón verde **"Crear Orden de Transporte"**.

### 3.3. Resultado de la Creación

Después de hacer clic, ocurrirán dos cosas:

1.  **Respuesta de la API:** En la parte inferior derecha, la sección "Respuesta de la API" mostrará el resultado.
    *   **Éxito (Estado 200 o 201):** La respuesta contendrá el ID de la orden creada por Dachser y la información de la etiqueta.
    *   **Error (Estado 4xx o 5xx):** Se mostrará un mensaje de error detallando el problema (ej: fecha inválida, dirección incorrecta, etc.).
2.  **Descarga de Etiqueta:** Si la orden se creó con éxito, el navegador iniciará automáticamente la descarga de un fichero PDF (`etiqueta_ORDEN_ID.pdf`). Esta es la etiqueta que debe ser pegada en la mercancía.

---

## 4. Gestionar Órdenes Existentes

La pestaña **"Listar Órdenes"** permite consultar, imprimir y eliminar órdenes ya creadas en el sistema de Dachser.

### 4.1. Buscar Órdenes

1.  Haz clic en la pestaña **"Listar Órdenes"**.
2.  Por defecto, los campos **"Fecha Desde"** y **"Fecha Hasta"** se rellenarán con la fecha del día actual. Puedes modificarlas para buscar en un rango de fechas diferente.
3.  Haz clic en el botón azul **"Buscar Órdenes"**.

El sistema mostrará una tabla con las órdenes encontradas en ese rango de fechas, incluyendo su ID, el número de albarán asociado, la fecha, el estado y el destinatario.

### 4.2. Acciones sobre una Orden

En la columna "Acciones" de la tabla de órdenes, tienes dos opciones:

*   **Imprimir:** Al hacer clic en este botón, se descargará de nuevo la etiqueta PDF correspondiente a esa orden. Es útil si necesitas reimprimir una etiqueta.
*   **Eliminar:**
    1.  Al hacer clic, aparecerá una ventana de confirmación.
    2.  Si confirmas, la orden será eliminada del sistema de Dachser. **Esta acción no se puede deshacer.**
    3.  La fila correspondiente desaparecerá de la tabla.

---

## 5. Resolución de Problemas Comunes

*   **Error "La fecha de transporte no puede ser anterior a la fecha actual"**: El albarán seleccionado tiene una fecha de envío pasada. No se puede crear la orden.
*   **Error "Falta un campo esperado..."**: Los datos del albarán en la base de datos están incompletos (ej: falta el código postal, la ciudad, etc.). Es necesario corregir los datos en el origen.
*   **La descarga de la etiqueta no se inicia**: Revisa la "Respuesta de la API". Si la creación de la orden falló, no habrá etiqueta para descargar. Si la orden se creó, puedes intentar descargarla de nuevo desde la pestaña "Listar Órdenes".