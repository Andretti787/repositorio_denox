# Manual de Usuario - Aplicación de Gestión de Pedidos

## 1. Introducción

Este manual describe el funcionamiento de la Aplicación de Gestión de Pedidos. El sistema permite a los usuarios crear, gestionar y visualizar pedidos de clientes de forma sencilla y eficiente.

## 2. Acceso al Sistema

### 2.1. Inicio de Sesión

Para acceder a la aplicación, es necesario contar con un usuario y contraseña.

1.  Abra la aplicación en su navegador.
2.  Ingrese su `código de usuario` y `contraseña` en los campos correspondientes.
3.  Haga clic en el botón **"Iniciar Sesión"**.

Si los datos son correctos, será redirigido a la pantalla principal. Si no, se mostrará un mensaje de error.

### 2.2. Cerrar Sesión

Para salir del sistema de forma segura:

1.  Haga clic en el enlace **"Cerrar Sesión"**, usualmente ubicado en la barra de navegación superior.
2.  Será redirigido a la página de inicio de sesión.

## 3. Pantalla Principal

La pantalla principal muestra una lista de todos los pedidos que ha creado, agrupados por número de pedido. Para cada pedido, se puede ver un resumen de la información de cabecera, como:

*   **Número de Pedido:** Identificador único del pedido.
*   **Cliente y Razón Social.**
*   **Fecha de Creación.**
*   **Estado:** La situación actual del pedido (ver sección 6).
*   **Tipo de Pedido:** Si es un pedido normal o una **Muestra**.

Desde esta pantalla, puede realizar las siguientes acciones:

*   **Crear un nuevo pedido:** Haciendo clic en el botón **"Nuevo Pedido"**.
*   **Ver el detalle de un pedido:** Haciendo clic en el botón **"Ver"** de un pedido existente.
*   **Editar un pedido:** Haciendo clic en el botón **"Editar"** (disponible solo para pedidos que no han sido procesados).
*   **Eliminar un pedido:** Haciendo clic en el botón **"Eliminar"**.

## 4. Gestión de Pedidos

### 4.1. Crear un Nuevo Pedido

1.  En la pantalla principal, haga clic en **"Nuevo Pedido"**.
2.  **Complete los Datos de Cabecera:**
    *   **Código Cliente:** Empiece a escribir el código o nombre del cliente. El sistema le mostrará sugerencias. Selecciónelo de la lista.
    *   **Código Dirección:** Una vez seleccionado el cliente, este campo se poblará con las direcciones de entrega disponibles para ese cliente. Seleccione la correcta.
    *   **Fecha Entrega:** La fecha en la que el cliente desea recibir el pedido.
    *   **Referencia Pedido Cliente:** Un campo opcional para registrar el número de pedido del cliente.
    *   **Descuento General (%):** Un descuento que se aplicará al total del pedido.
    *   **Observaciones:** Cualquier nota o comentario relevante sobre el pedido (máximo 30 caracteres).
3.  **Añada Líneas de Pedido:**
    *   En la sección "Líneas del Pedido", comience a escribir el código o la descripción de un artículo en el campo **"Artículo"**. El sistema le mostrará sugerencias con el stock disponible y el precio.
    *   Al seleccionar un artículo, los campos de descripción y precio se rellenarán automáticamente.
    *   Ingrese la **Cantidad** deseada. El sistema validará si la cantidad es un múltiplo de la unidad de embalaje del artículo.
    *   Puede añadir un **descuento de línea** específico para ese artículo.
    *   Para añadir más artículos, haga clic en el botón **"Añadir Línea"**.
4.  **Guardar como Borrador:** Si marca la casilla **"Guardar como BORRADOR"**, el pedido se guardará con este estado y no será procesado hasta que se edite y se guarde como "Pendiente".
5.  **Crear Pedido de Muestra:** Si marca la casilla **"Pedido de Muestra (sin cargo)"**, todos los precios del pedido se establecerán a cero. El pedido se marcará internamente como una muestra.
6.  Haga clic en **"Crear Pedido"** para finalizar.

### 4.2. Ver un Pedido

Desde la lista principal, haga clic en el botón **"Ver"** del pedido que desea consultar. Se mostrará una pantalla con todos los detalles de la cabecera y las líneas, así como el importe total. Si el pedido es una muestra, se indicará claramente y los importes serán cero.

### 4.3. Editar un Pedido

Solo los pedidos que **no** estén en estado "PROCESADO" pueden ser editados.

1.  Desde la lista principal, haga clic en el botón **"Editar"** del pedido correspondiente.
2.  Se cargará el formulario de pedido con toda la información existente.
3.  Modifique los campos que necesite, ya sea en la cabecera o en las líneas. Puede añadir o eliminar líneas.
4.  Haga clic en **"Actualizar Pedido"** para guardar los cambios.

### 4.4. Eliminar un Pedido

1.  Desde la lista principal, localice el pedido que desea eliminar.
2.  Haga clic en el botón **"Eliminar"**.
3.  El sistema le pedirá confirmación antes de borrar el pedido y todas sus líneas de forma permanente.

## 5. Glosario de Estados del Pedido

*   **BORRADOR:** El pedido ha sido guardado pero no está finalizado. No será procesado por el sistema central. Puede ser editado y completado más tarde.
*   **PENDIENTE:** El pedido está finalizado y listo para ser procesado por el sistema central.
*   **BLOQUEADO:** El pedido ha sido bloqueado por alguna razón (ej. riesgo de cliente) y requiere revisión manual.
*   **PROCESADO:** El pedido ha sido recibido y procesado por el sistema central. Ya no se puede editar ni eliminar desde esta aplicación.
*   **ENVIADO:** El pedido ha sido enviado al cliente.
*   **ENTREGADO:** El pedido ha sido entregado al cliente.
*   **CANCELADO:** El pedido ha sido cancelado.
