# --- Fase 1: Definir la imagen base ---
# Usamos una imagen base de Rocky Linux 9 (compatible con RHEL/CentOS) y luego instalamos Python.
FROM rockylinux:9-minimal

# --- Fase 2: Instalar dependencias del sistema operativo ---
# Instalar Python y herramientas necesarias. 'dnf' es el gestor de paquetes en RHEL/CentOS 9.
# Habilitamos el repositorio 'crb' para poder instalar 'unixODBC-devel'.
RUN microdnf --enablerepo=crb install -y python3.9 python3.9-pip curl gzip tar unixODBC-devel mariadb-devel && microdnf clean all

# Instalar el driver ODBC de Microsoft para SQL Server en un entorno RHEL/CentOS
RUN curl -fsSL https://packages.microsoft.com/config/rhel/9.0/prod.repo > /etc/yum.repos.d/mssql-release.repo

# Instalar el driver aceptando la EULA
RUN ACCEPT_EULA=Y microdnf install -y msodbcsql17 && microdnf clean all

# --- Fase 3: Configurar el entorno de la aplicación ---

# Variables de entorno por defecto (se pueden sobreescribir en runtime).
# Evita que Python genere ficheros .pyc y fuerza salida sin buffer (mejores logs en contenedor).
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    FLASK_DEBUG=false \
    FLASK_HOST=0.0.0.0 \
    FLASK_PORT=5002

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# --- Fase 4: Instalar dependencias de Python ---

# Copiar primero el fichero de requisitos para aprovechar el cache de Docker
COPY requirements.txt .

# Instalar las librerías de Python (incluye gunicorn como servidor de producción)
RUN python3.9 -m pip install --no-cache-dir -r requirements.txt

# --- Fase 5: Copiar el código de la aplicación ---
# El fichero .env y otros secretos quedan excluidos mediante .dockerignore.
# Las credenciales deben inyectarse en runtime (docker run --env-file .env o variables de entorno).
COPY . .

# --- Fase 6: Crear un usuario sin privilegios y ejecutar como él ---
# Ejecutar como root es un riesgo de seguridad; creamos un usuario dedicado.
RUN useradd --create-home --uid 1001 appuser && chown -R appuser:appuser /app
USER appuser

# --- Fase 7: Exponer el puerto y definir el comando de ejecución ---
# Exponer el puerto 5002, que es el que usa la aplicación.
EXPOSE 5002

# Ejecutar la aplicación con gunicorn (servidor WSGI de producción) en lugar del
# servidor de desarrollo de Flask. 'transporte:app' = módulo:objeto Flask.
# El bind usa el puerto 5002 fijo (puerto interno del contenedor); el mapeo al
# host se hace con la opción -p de 'docker run'. Se usa un puerto fijo para evitar
# problemas si FLASK_PORT llegara con comillas u otro formato desde --env-file.
CMD ["gunicorn", "--bind", "0.0.0.0:5002", "--workers", "3", "--timeout", "60", "transporte:app"]


