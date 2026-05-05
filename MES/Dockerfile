# Usa una imagen base de Python ligera
FROM python:3.9-slim-bullseye

# Configura variables de entorno para Flask
ENV PYTHONUNBUFFERED=1
ENV FLASK_APP=app_mes.py
ENV FLASK_RUN_HOST=0.0.0.0
ENV FLASK_RUN_PORT=5004

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Instala las dependencias necesarias para el controlador ODBC 17 para SQL Server
# Esta parte es crucial para que pyodbc pueda conectarse a SQL Server
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        gnupg \
        unixodbc-dev \
        tzdata \
    && curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - \
    && curl https://packages.microsoft.com/config/debian/11/prod.list > /etc/apt/sources.list.d/mssql-release.list \
    && apt-get update \
    && ACCEPT_EULA=Y apt-get install -y --no-install-recommends msodbcsql17 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copia el archivo de requisitos e instala las dependencias de Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia el código de la aplicación (app_mes.py y la carpeta templates)
COPY . .

# Expone el puerto en el que se ejecutará Flask
EXPOSE 5004

# Comando para ejecutar la aplicación Flask
CMD ["flask", "run"]