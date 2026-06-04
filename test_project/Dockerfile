# Dockerfile para CentOS Stream 9
# Aplicación Web de Resumen de Pedidos

# Usar imagen base de Python oficial (basada en Debian)
FROM python:3.11-slim

# Metadatos
LABEL maintainer="mmarco"
LABEL description="Aplicación Web de Resumen de Pedidos - Flask + MySQL"
LABEL version="1.0.0"

# Establecer variables de entorno
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV PIP_NO_CACHE_DIR=1
ENV PIP_DISABLE_PIP_VERSION_CHECK=1

# Instalar dependencias del sistema
RUN apt-get update && \
    apt-get install -y \
    default-libmysqlclient-dev \
    gcc \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de requisitos
COPY requirements.txt .

# Instalar dependencias de Python
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el resto de la aplicación
COPY . .

# Crear directorios para datos temporales
RUN mkdir -p /app/data

# Exponer puerto
EXPOSE 5000

# Usuario no root por seguridad
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# Comando de inicio
CMD ["python3", "app.py"]