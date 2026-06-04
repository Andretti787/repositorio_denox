# DAL-IA - Gestión de Fichas de Paletización

Aplicación web para la gestión de fichas de paletización, convertida desde una plantilla HTML estática a una aplicación full-stack con base de datos.

## Características

- **CRUD completo**: Crear, leer, actualizar y eliminar fichas de paletización
- **Base de datos MySQL**: Almacenamiento persistente en servidor remoto
- **Autenticación**: Sistema de login seguro
- **Interfaz imprimible**: Diseño optimizado para impresión/PDF
- **API REST**: Endpoints JSON para integración
- **Logo personalizable**: Subida y visualización de logos de empresa
- **Integración con DIM_CTE**: Autocompletado de clientes desde dwdb.DIM_CTE
- **Diseño responsive**: Funciona en desktop y móvil

## Instalación

### Requisitos previos

- Python 3.8 o superior
- pip (gestor de paquetes de Python)

### Pasos de instalación

1. **Clonar o descargar el proyecto**

```bash
cd dal_ia
```

2. **Crear entorno virtual (recomendado)**

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Linux/Mac
python3 -m venv venv
source venv/bin/activate
```

3. **Instalar dependencias**

```bash
pip install -r requirements.txt
```

4. **Configurar variables de entorno**

El archivo `.env` ya incluye valores por defecto. Puedes modificarlo si necesitas cambiar:
- `SECRET_KEY`: Clave secreta para sesiones
- `APP_USER`: Usuario de la aplicación (por defecto: dilies)
- `APP_PASSWORD`: Contraseña (por defecto: dalia123)
- `PORT`: Puerto del servidor (por defecto: 5005)

5. **Ejecutar la aplicación**

```bash
# Modo desarrollo (con auto-reload)
python app.py

# O usando Flask CLI
flask run --port=5001
```

6. **Acceder a la aplicación**

Abre tu navegador y ve a: `http://localhost:5005`

## Uso

### Primer acceso

- **Usuario**: dilies
- **Contraseña**: dalia123

(Puedes cambiar estas credenciales en el archivo `.env`)

### Crear una ficha

1. Haz clic en "Nueva Ficha" en la página principal
2. Completa los datos del cliente
3. Configura las opciones de preparación de mercancía
4. Establece las especificaciones de paletización
5. Completa la sección de etiquetado y documentación
6. Añade observaciones si es necesario
7. (Opcional) Sube un logo de la empresa
8. Haz clic en "Crear Ficha"

### Ver/Imprimir una ficha

1. Desde el listado, haz clic en "Ver" en cualquier ficha
2. Para imprimir o guardar como PDF, haz clic en el botón "Imprimir / Guardar PDF"
3. En el diálogo de impresión, selecciona "Guardar como PDF" como destino

### Editar una ficha

1. Desde el detalle de la ficha, haz clic en "Editar"
2. Modifica los campos necesarios
3. Haz clic en "Guardar Cambios"

### Eliminar una ficha

1. Desde el listado, haz clic en "Eliminar" en la ficha deseada
2. Confirma la eliminación

## API Endpoints

La aplicación incluye una API REST para integración con otros sistemas:

### Health Check
```
GET /api/health
```

### Listar fichas
```
GET /api/fichas
```

### Obtener ficha específica
```
GET /api/ficha/<id>
```

### Crear ficha (JSON)
```
POST /api/ficha/crear
Content-Type: application/json

{
  "cliente": "Empresa XYZ",
  "numero_cliente": "12345",
  "destino": "Madrid"
}
```

## Estructura del proyecto

```
dal_ia/
├── app.py                 # Aplicación Flask principal
├── database.py            # Módulo de base de datos
├── requirements.txt       # Dependencias de Python
├── .env                   # Variables de entorno
├── README.md              # Este archivo
├── templates/             # Plantillas HTML
│   ├── login.html         # Página de login
│   ├── index.html         # Listado de fichas
│   ├── ficha_form.html    # Formulario de creación/edición
│   └── ficha_ver.html     # Vista detallada/imprimible
└── static/                # Archivos estáticos (CSS, JS, imágenes)
```

## Base de datos

La aplicación utiliza MySQL como base de datos. La tabla se crea automáticamente en el primer inicio.

### Configuración de la base de datos

Los parámetros de conexión se configuran en el archivo `.env`:
- `DB_HOST`: Servidor MySQL (por defecto: 192.168.35.25)
- `DB_PORT`: Puerto (por defecto: 3306)
- `DB_NAME`: Nombre de la base de datos (por defecto: dalia)
- `DB_USER`: Usuario (por defecto: mmarco)
- `DB_PASSWORD`: Contraseña

### Tabla principal: `fichas_paletizacion`

Campos principales:
- `id`: Identificador único
- `cliente`, `numero_cliente`, `destino`: Datos del cliente
- `empaquetado_individual`, `etiquetado_especial`, etc.: Opciones de preparación
- `altura_maxima`, `peso_maximo`, `tipo_palet`: Especificaciones técnicas
- `lleva_carteles`, `etiquetas`, `codigo_sscc`, etc.: Requisitos de etiquetado
- `observaciones`: Texto libre para notas adicionales
- `logo_data`: Logo en formato base64
- `fecha_creacion`, `fecha_modificacion`: Timestamps

### Integración con DIM_CTE

La aplicación se conecta a la tabla `dwdb.DIM_CTE` para obtener la lista de clientes:
- `CTE_COD`: Código del cliente (se usa como número de cliente)
- `CTE_RAZON_SOCIAL`: Razón social del cliente (se usa como nombre del cliente)

Al seleccionar un cliente en el formulario, el campo "Cliente" se autocompleta automáticamente.

## Despliegue con Docker (CentOS 10 / Rocky Linux 10)

La aplicación incluye archivos para contenerización en servidores CentOS 10 o compatibles (Rocky Linux 10, AlmaLinux 10).

### Archivos Docker incluidos:
- `Dockerfile` - Configuración del contenedor basado en Rocky Linux 10
- `docker-compose.yml` - Orquestación del contenedor
- `.dockerignore` - Optimización del build

### Instrucciones de despliegue:

1. **Construir y ejecutar con docker-compose:**
```bash
cd dal_ia
docker-compose up -d --build
```

2. **O construir manualmente la imagen:**
```bash
docker build -t dal-ia:latest .
docker run -d -p 5001:5001 --name dal-ia-app dal-ia:latest
```

3. **Ver logs:**
```bash
docker-compose logs -f
# o
docker logs -f dal-ia-app
```

4. **Detener el contenedor:**
```bash
docker-compose down
# o
docker stop dal-ia-app
```

### Configuración de variables de entorno:

Puedes personalizar la configuración creando un archivo `.env` en el mismo directorio que `docker-compose.yml`:

```bash
# Copiar el .env existente o crear uno nuevo
cp .env .env.production

# Editar con los valores deseados
DB_HOST=tu_servidor_mysql
DB_USER=tu_usuario
DB_PASSWORD=tu_contraseña
APP_USER=dilies
APP_PASSWORD=dalia123
SECRET_KEY=tu_clave_secreta_muy_segura
```

## Tecnologías utilizadas

- **Flask 3.0**: Framework web Python
- **MySQL**: Base de datos relacional
- **mysql-connector-python**: Conector MySQL para Python
- **HTML5/CSS3**: Estructura y estilos
- **JavaScript**: Interactividad del lado del cliente
- **Waitress**: Servidor WSGI para producción
- **Docker**: Contenerización

## Licencia

Este proyecto es de uso interno.

## Soporte

Para incidencias o consultas, contacta con el equipo de desarrollo.
