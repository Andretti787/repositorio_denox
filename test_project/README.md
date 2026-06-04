# Aplicación Web de Resumen de Pedidos

Aplicación web desarrollada en Flask que muestra un resumen de pedidos desde una base de datos MySQL, diseñada para ejecutarse en contenedores Docker sobre CentOS Stream 10.

## Características

- ✅ **Visualización de datos**: Muestra resumen de pedidos por usuario
- ✅ **Navegación jerárquica**: Permite hacer clic en las filas para ver el detalle
- ✅ **Interfaz moderna**: Diseño responsive con tabla interactiva
- ✅ **Exportación CSV**: Permite descargar los datos en formato CSV
- ✅ **Actualización en tiempo real**: Botón para recargar datos manualmente
- ✅ **Indicador de estado**: Muestra el estado de conexión a la base de datos
- ✅ **Contenedor Docker**: Listo para desplegar en CentOS 10
- ✅ **Solo lectura**: Todos los datos son de consulta, no se permite modificación

## Requisitos del Sistema

- **Servidor**: CentOS Stream 10
- **Base de datos**: MySQL 5.7+ o MariaDB 10.3+
- **Docker**: Docker 20.10+ y Docker Compose 2.0+

## Estructura del Proyecto

```
test_project/
├── app.py                 # Aplicación Flask principal
├── database.py            # Módulo de conexión a MySQL
├── requirements.txt       # Dependencias de Python
├── .env                   # Variables de entorno (configuración)
├── .gitignore             # Archivos a ignorar en Git
├── Dockerfile             # Configuración Docker para CentOS 10
├── README.md              # Este archivo
├── templates/
│   ├── index.html         # Página principal (resumen)
│   ├── pedidos_list.html  # Lista de pedidos por usuario/estado
│   └── pedido_detalle.html # Detalle completo de un pedido
└── static/
    ├── css/
    │   └── style.css      # Estilos de la aplicación
    └── js/
        └── app.js         # Lógica frontend
```

## Instalación y Configuración

### 1. Clonar o copiar el proyecto

```bash
# Copiar el proyecto al servidor CentOS
scp -r test_project usuario@servidor:/ruta/destino/
```

### 2. Configurar variables de entorno

Editar el archivo `.env` con los datos de tu base de datos:

```bash
cd test_project
nano .env
```

Variables a configurar:
```env
DB_HOST=192.168.35.25        # IP del servidor MySQL
DB_PORT=3306                  # Puerto MySQL
DB_USER=mmarco                # Usuario de la base de datos
DB_PASSWORD=tu_password       # Contraseña del usuario
DB_NAME=pract                 # Nombre de la base de datos
FLASK_ENV=production          # Entorno (production/development)
SECRET_KEY=tu_clave_secreta   # Clave secreta para Flask
HOST=0.0.0.0                  # Host de escucha
PORT=5000                     # Puerto de la aplicación
```

### 3. Construir y ejecutar con Docker

```bash
# Construir la imagen Docker
docker build -t pedidos-web:1.0 .

# Ejecutar el contenedor
docker run -d \
  --name pedidos-app \
  -p 5000:5000 \
  --restart unless-stopped \
  pedidos-web:1.0
```

### 4. Verificar que está funcionando

```bash
# Ver logs del contenedor
docker logs pedidos-app

# Verificar endpoint de salud
curl http://localhost:5000/api/health
```

## Uso con Docker Compose (Recomendado)

Crear archivo `docker-compose.yml`:

```yaml
version: '3.8'

services:
  pedidos-web:
    build: .
    container_name: pedidos-app
    ports:
      - "5000:5000"
    env_file:
      - .env
    restart: unless-stopped
    networks:
      - pedidos-network

networks:
  pedidos-network:
    driver: bridge
```

Ejecutar con Docker Compose:

```bash
docker-compose up -d
```

## Acceso a la Aplicación

Una vez ejecutándose, acceder a:

```
http://servidor:5000
```

## Endpoints API

### Página Principal
```
GET /
```
Muestra la interfaz web con la tabla de resumen de pedidos.

### Lista de Pedidos por Usuario
```
GET /pedidos/<usuario>
```
Muestra la lista de pedidos para un usuario específico.

### Detalle de Pedido
```
GET /pedido/<usuario>/<numped>
```
Muestra el detalle completo de un pedido específico con todas sus líneas.

### API de Pedidos (JSON)
```
GET /api/pedidos
```
Devuelve los datos en formato JSON.

**Respuesta exitosa:**
```json
{
  "success": true,
  "data": [
    {
      "usuario": "004",
      "nombre": "Juan Pérez",
      "num_pedidos": 5,
      "importe_total": 1250.50,
      "importe_medio": 250.10
    }
  ],
  "total_registros": 1
}
```

### API de Pedidos por Usuario (JSON)
```
GET /api/pedidos/<usuario>
```
Devuelve la lista de pedidos para un usuario específico.

### API de Detalle de Pedido (JSON)
```
GET /api/pedido/<usuario>/<numped>
```
Devuelve el detalle completo de un pedido con todas sus líneas.

### Health Check
```
GET /api/health
```
Verifica el estado de la aplicación.

**Respuesta:**
```json
{
  "status": "OK",
  "database": "Conectado",
  "version": "1.0.0"
}
```

## Consultas SQL Utilizadas

La aplicación ejecuta la siguiente consulta SQL para obtener los datos:

```sql
SELECT 
    COALESCE(DATOS.USUARIO, 'TOTAL GENERAL') AS USUARIO,
    COALESCE(USU.NOMBRE, '') AS NOMBRE,
    COUNT(*) AS NUM_PEDIDOS,
    SUM(DATOS.IMPORTE) AS IMPORTE_TOTAL,
    AVG(DATOS.IMPORTE) AS IMPORTE_MEDIO_POR_PEDIDO
FROM 
(
    SELECT USUARIO, NUMPED,
           COUNT(*) AS LINEAS, 
           SUM(CANTIDAD * PRECIO) AS IMPORTE 
    FROM pract.PEDIDOS_WEB
    WHERE USUARIO NOT IN ('001', '002', '003')
    AND PRECIO > 0
    GROUP BY USUARIO, NUMPED
) AS DATOS
INNER JOIN pract.USUARIOS_LOGIN USU ON USU.COD_USER = DATOS.USUARIO
GROUP BY DATOS.USUARIO, USU.NOMBRE
ORDER BY USUARIO, IMPORTE_TOTAL DESC
```

## Gestión del Contenedor

### Detener la aplicación
```bash
docker stop pedidos-app
```

### Iniciar la aplicación
```bash
docker start pedidos-app
```

### Reiniciar la aplicación
```bash
docker restart pedidos-app
```

### Ver logs en tiempo real
```bash
docker logs -f pedidos-app
```

### Eliminar la aplicación
```bash
docker stop pedidos-app
docker rm pedidos-app
docker rmi pedidos-web:1.0
```

## Seguridad

### Consideraciones importantes:

1. **Contraseñas**: Cambiar la contraseña por defecto en producción
2. **SECRET_KEY**: Generar una clave secreta única para Flask
3. **Firewall**: Asegurar que solo los puertos necesarios estén abiertos
4. **HTTPS**: Usar un proxy inverso (nginx) con SSL/TLS en producción
5. **Base de datos**: Restringir acceso a la BD solo desde la IP del servidor

### Configuración recomendada para producción:

```bash
# 1. Generar SECRET_KEY segura
python3 -c "import secrets; print(secrets.token_hex(32))"

# 2. Actualizar .env con la clave generada
# 3. Configurar firewall (firewalld en CentOS)
sudo firewall-cmd --permanent --add-port=5000/tcp
sudo firewall-cmd --reload
```

## Solución de Problemas

### Error: "No se pudo conectar a la base de datos"

1. Verificar que el servidor MySQL esté accesible:
```bash
mysql -h 192.168.35.25 -u mmarco -p
```

2. Comprobar que el usuario tenga permisos:
```sql
SHOW GRANTS FOR 'mmarco'@'%';
```

3. Verificar que las tablas existan:
```sql
USE pract;
SHOW TABLES;
```

### Error: "Puerto ya en uso"

Cambiar el puerto en `.env`:
```env
PORT=5001
```

Y reconstruir el contenedor:
```bash
docker-compose down
docker-compose up -d
```

### Error: "Permission denied"

Asegurar que los archivos tengan permisos correctos:
```bash
sudo chown -R $USER:$USER test_project
chmod -R 755 test_project
```

## Actualización de la Aplicación

Para actualizar la aplicación a una nueva versión:

```bash
# 1. Detener contenedor actual
docker-compose down

# 2. Actualizar código (git pull o copiar nuevos archivos)
git pull origin main

# 3. Reconstruir imagen
docker-compose build --no-cache

# 4. Iniciar nueva versión
docker-compose up -d

# 5. Verificar logs
docker-compose logs -f
```

## Backup de Datos

La aplicación no almacena datos localmente, todos los datos vienen de MySQL. Sin embargo, es recomendable hacer backup de:

1. **Archivo .env**: Contiene la configuración
2. **Base de datos MySQL**:
```bash
mysqldump -h 192.168.35.25 -u mmarco -p pract > backup_pract_$(date +%Y%m%d).sql
```

## Soporte y Mantenimiento

### Monitoreo básico:

```bash
# Ver uso de recursos del contenedor
docker stats pedidos-app

# Verificar que el proceso esté corriendo
docker ps | grep pedidos-app

# Probar endpoint de salud
curl http://localhost:5000/api/health
```

### Logs de errores:

```bash
# Ver últimos 100 logs
docker logs --tail 100 pedidos-app

# Buscar errores específicos
docker logs pedidos-app | grep -i error
```

## Licencia

Este proyecto es de uso interno y no está sujeto a licencia abierta.

---

**Versión**: 1.0.0  
**Última actualización**: Mayo 2026  
**Desarrollador**: mmarco