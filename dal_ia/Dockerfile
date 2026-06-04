# Dockerfile para DAL-IA - CentOS 10
# Imagen base: Rocky Linux 10 (compatible con CentOS Stream 10)
FROM rockylinux/rockylinux:10

# Metadatos
LABEL maintainer="DAL-IA Team"
LABEL description="Aplicación web para gestión de fichas de paletización"
LABEL version="1.0"

# Establecer zona horaria
ENV TZ=Europe/Madrid
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Instalar dependencias del sistema
RUN dnf install -y \
    python3.12 \
    python3.12-pip \
    python3.12-devel \
    gcc \
    && dnf clean all

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de requisitos primero (para aprovechar cache de Docker)
COPY requirements.txt .

# Instalar dependencias de Python
RUN pip3 install --no-cache-dir -r requirements.txt

# Copiar el resto de la aplicación
COPY . .

# Crear usuario no root para mayor seguridad
RUN useradd -m -u 1000 dalapp && chown -R dalapp:dalapp /app
USER dalapp

# Exponer puerto
EXPOSE 5005

# Variables de entorno por defecto (pueden ser sobrescritas)
ENV HOST=0.0.0.0
ENV PORT=5005
ENV FLASK_ENV=production

# Comando de inicio usando Waitress (WSGI production server)
CMD ["python3", "app.py"]