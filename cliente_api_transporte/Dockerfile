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

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# --- Fase 4: Instalar dependencias de Python ---

# Copiar primero el fichero de requisitos para aprovechar el cache de Docker
COPY requirements.txt .

# Instalar las librerías de Python
RUN python3.9 -m pip install --no-cache-dir -r requirements.txt

# --- Fase 5: Copiar el código de la aplicación ---
COPY . .

# --- Fase 6: Exponer el puerto y definir el comando de ejecución ---
# Exponer el puerto 5002, que es el que has configurado en transporte.py
EXPOSE 5002

# Comando para ejecutar la aplicación cuando el contenedor se inicie
CMD ["python3.9", "transporte.py"]