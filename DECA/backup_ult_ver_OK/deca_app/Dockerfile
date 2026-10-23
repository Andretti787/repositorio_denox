# --- Fase 1: Imagen base ---
# Rocky Linux 9 minimal (mismo patron que el resto de proyectos Docker del entorno)
FROM rockylinux:9-minimal

# --- Fase 2: Dependencias del sistema operativo ---
# Python 3.11 + unixODBC-devel (necesario para compilar pyodbc) + curl (healthcheck)
# + dejavu-sans-fonts (acentos correctos en el PDF con ReportLab)
RUN microdnf --enablerepo=crb install -y python3.11 python3.11-pip curl unixODBC-devel dejavu-sans-fonts && microdnf clean all

# Driver ODBC oficial de Microsoft para SQL Server
RUN curl -fsSL https://packages.microsoft.com/config/rhel/9.0/prod.repo > /etc/yum.repos.d/mssql-release.repo
RUN ACCEPT_EULA=Y microdnf install -y msodbcsql17 && microdnf clean all

# --- Fase 3: Configuracion del entorno ---
# Sin ficheros .pyc y salida sin buffer (mejores logs en contenedor)
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    APP_PORT=5010

WORKDIR /app

# --- Fase 4: Dependencias de Python (capa cacheada) ---
COPY requirements.txt .
RUN python3.11 -m pip install --no-cache-dir -r requirements.txt

# --- Fase 5: Codigo de la aplicacion ---
# El fichero .env y otros secretos quedan excluidos mediante .dockerignore;
# las credenciales se inyectan en runtime (env_file en docker-compose).
COPY . .

# --- Fase 6: Usuario sin privilegios ---
# 1) Crear el usuario no privilegiado que ejecutará la aplicacion.
# 2) Crear el directorio de PDF con permisos del usuario appuser.
#    Esto evita el PermissionError al generar DeCA.
RUN useradd --create-home --uid 1001 appuser \
    && mkdir -p /app/deca_pdf \
    && chown -R appuser:appuser /app
USER appuser

# --- Fase 7: Puerto y comando de arranque ---
EXPOSE 5010
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5010", "--workers", "2"]