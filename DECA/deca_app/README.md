# DeCA - Gestion del Documento electronico de Control Administrativo

Aplicacion web (FastAPI + interfaz HTML/JS) que **lista los albaranes de transporte
almacenados en SQL Server (Sage X3)**, permite completar **solo los cuatro campos
editables del DeCA**, genera el **PDF nativo con codigo QR**, lo **sube por HTTPS** al
endpoint corporativo `https://denox.eu/DECA/upload_deca.php` y guarda la URL devuelta
en la base de datos. Tambien permite descargar el PDF o enviarlo al conductor por email.

Cumple los requisitos de la Resolucion de 5 de junio de 2026, la disposicion transitoria
8a de la Ley de Movilidad Sostenible y la Orden FOM/2861/2012.

---

## 1. Estructura del proyecto

```
deca_app/
  Dockerfile                  Imagen Rocky Linux 9 + Python 3.11 + msodbcsql17
  docker-compose.yml          Servicio deca-app (puerto 5010) + variables de entorno
  requirements.txt            Dependencias Python
  .env.example                Plantilla de variables de entorno
  sql/create_zdeca.sql        Script de creacion de la tabla LIVE.ZDECA
  app/
    main.py                   Arranque FastAPI, /health, estaticos
    config.py                 Configuracion desde variables de entorno
    database.py               Pool pyodbc + consultas SQL Server (SDELIVERY / ZDECA)
    validators.py             Validacion de matriculas espanolas
    models.py                 Esquemas Pydantic (entrada/salida)
    routers/albaranes.py      GET /albaranes, PUT /albaranes/{id}, POST /albaranes/{id}/deca...
    services/pdf_service.py   PDF nativo (ReportLab) + QR + metadatos + limite 5 MB
    services/upload_service.py Subida multipart HTTPS (TLS >= 1.2)
    services/email_service.py  Envio del PDF al conductor por SMTP
  static/
    index.html, app.js, style.css   Interfaz con tabla editable inline
```

## 2. Requisitos previos

- Docker Engine 20.10+ y Docker Compose v2.
- Acceso de red desde el contenedor a SQL Server (puerto 1433 o el configurado).
- Dominio `denox.eu` accesible por HTTPS con certificado valido (la subida exige HTTPS).
- Tabla `LIVE.ZDECA` creada (ejecutar `sql/create_zdeca.sql`).

## 3. Despliegue con Docker

```bash
# 1) Crear el fichero de entorno a partir de la plantilla
cp .env.example .env      # En Windows: Copy-Item .env.example .env

# 2) Editar .env con los valores reales (SIN comillas). Ejemplo:
#    DB_SERVER=192.168.35.10,1433
#    DB_DATABASE=X3
#    DB_USERNAME=usuario
#    DB_PASSWORD=contrasena
#    DB_DRIVER={ODBC Driver 17 for SQL Server}
#    DECA_UPLOAD_URL=https://denox.eu/DECA/upload_deca.php
#    DECA_BASE_URL=https://denox.eu/DECA/deca_storage/

# 3) Levantar la aplicacion
docker compose up -d --build

# 4) Comprobar estado y logs
docker compose ps
docker compose logs -f deca-app

# 5) Interfaz y documentacion de la API
#    http://localhost:5010          -> tabla de albaranes editable
#    http://localhost:5010/docs     -> Swagger UI
#    http://localhost:5010/health   -> estado
```

Para detenerla: `docker compose down`.

## 4. Variables de entorno

| Variable | Obligatoria | Descripcion |
|---|---|---|
| `DB_SERVER` | Si | Servidor SQL Server en formato `IP,PUERTO` o `SERVIDOR\INSTANCIA` |
| `DB_DATABASE` | Si | Nombre de la base de datos (Sage X3) |
| `DB_USERNAME` / `DB_PASSWORD` | Si | Credenciales de SQL Server |
| `DB_DRIVER` | No | Por defecto `{ODBC Driver 17 for SQL Server}` |
| `DB_TIMEOUT` | No | Segundos de espera al conectar con SQL Server (por defecto 5). Subirlo si la red es lenta |
| `DECA_UPLOAD_URL` | Si | Endpoint de subida (debe ser HTTPS) |
| `DECA_BASE_URL` | Si | URL publica base de los PDF. **Debe coincidir con `$baseUrl` de `upload_deca.php`** |
| `SMTP_HOST` / `SMTP_PORT` | No | Servidor SMTP para el envio por email (465 = TLS implicito) |
| `SMTP_USER` / `SMTP_PASSWORD` | No | Credenciales SMTP |
| `SMTP_FROM` | No | Remitente de los correos del DeCA |
| `DECA_CARGADOR_NOMBRE` | No | Nombre del cargador contractual (si esta vacio se usa el sitio de carga del albaran) |
| `DECA_CARGADOR_NIF` | Recomendado | NIF del cargador contractual (campo obligatorio del DeCA) |
| `DECA_CARGADOR_DOMICILIO` | Recomendado | Domicilio del cargador contractual (campo obligatorio del DeCA) |
| `ALBARANES_LIMITE_MAX` | No | Maximo de filas que admite el listado (por defecto 1000) |
| `APP_PORT` | No | Puerto interno/externo (por defecto 5010) |
| `PDF_DIR` | No | Directorio local con copia de los PDF (por defecto `deca_pdf`) |

## 5. Esquema de datos

Los datos del albaran se leen directamente de SQL Server (base `x3`) con el siguiente mapeo,
verificado sobre el esquema real:

| Campo del DeCA | Origen en Sage X3 |
|---|---|
| Numero de albaran | `LIVE.SDELIVERY.SDHNUM_0` |
| Fecha de realizacion del transporte | `LIVE.SDELIVERY.SHIDAT_0` |
| Cargador contractual (nombre) | `LIVE.FACILITY.FCYNAM_0` del sitio de carga (`STOFCY_0`); se puede fijar con `DECA_CARGADOR_NOMBRE` |
| Cargador contractual (NIF y domicilio) | Variables `DECA_CARGADOR_NIF` y `DECA_CARGADOR_DOMICILIO` (son constantes de la empresa) |
| Destinatario | `LIVE.BPCUSTOMER.BPCNAM_0` del cliente del albaran (`BPCORD_0`) |
| Transportista efectivo | `LIVE.BPARTNER.BPRNAM_0` o `LIVE.BPCARRIER.BPTNAM_0` del transportista (`BPTNUM_0`) |
| NIF del transportista efectivo | `LIVE.BPARTNER.CRN_0` |
| Origen | Sitio de carga: `LIVE.FACILITY` (`FCY_0` = `SDELIVERY.STOFCY_0`) |
| Destino | `SDELIVERY.BPDCTY_0`, `BPDPOSCOD_0` y `BPDCRYNAM_0`/`BPDCRY_0` (direccion de entrega) |
| Naturaleza de la mercancia | Hasta 3 descripciones `LIVE.ITMMASTER.ITMDES1_0` de las lineas de `LIVE.SDELIVERYD` |
| Peso de la mercancia | `LIVE.SDELIVERY.GROWEI_0` (kg) |
| Observaciones | `LIVE.BPDLVCUST.DLVTEX_0` -> `LIVE.TEXCLOB.TEXTE_0` |
| Matriculas y cambios (4 campos editables) | `LIVE.ZDECA` (editables en la interfaz) |

Los campos editables y el estado del DeCA se guardan en `LIVE.ZDECA`:

| Columna | Tipo | Uso |
|---|---|---|
| `SDHNUM_0` | VARCHAR(20) PK | Numero de albaran (`SDELIVERY.SDHNUM_0`) |
| `ZMATTRA_0` | NVARCHAR(15) | Matricula del vehiculo tractor |
| `ZMATREM_0` | NVARCHAR(15) | Matricula del remolque |
| `ZCAMTRA_0` | NVARCHAR(15) | Cambio de matricula del vehiculo tractor |
| `ZCAMREM_0` | NVARCHAR(15) | Cambio de matricula del remolque |
| `ZDECAURL_0` | NVARCHAR(500) | URL publica del DeCA generado |
| `ZDECAEST_0` | SMALLINT | 0 = pendiente, 1 = DeCA generado |
| `CREDAT_0` / `UPDDAT_0` | DATE | Fecha de creacion / modificacion del registro |

> Si las columnas de tu Sage X3 difieren, ajusta la consulta `_SQL_BASE` de
> `app/database.py` (los JOIN y alias estan comentados y agrupados para facilitarlo).

## 6. API REST

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/albaranes?solo_pendientes=true&limite=200&desplazamiento=0&filtro=texto` | Lista de albaranes con los datos de solo lectura y los 4 campos editables. El historico tiene decenas de miles de filas: use `limite` (por defecto 200, maximo `ALBARANES_LIMITE_MAX` = 1000), `desplazamiento` para paginar y `filtro` para buscar por numero de albaran o destinatario |
| PUT | `/albaranes/{numalbaran}` | Valida y guarda (UPSERT en `LIVE.ZDECA`) los 4 campos editables |
| POST | `/albaranes/{numalbaran}/deca` | Genera el PDF con QR, lo sube por HTTPS y guarda la URL |
| GET | `/albaranes/{numalbaran}/deca/pdf` | Descarga local del PDF generado |
| POST | `/albaranes/{numalbaran}/deca/email` | Envia el PDF al email indicado |

### Ejemplo de uso con curl

```bash
# 1) Listar albaranes pendientes
curl -s "http://localhost:5010/albaranes?solo_pendientes=true"

# 2) Guardar las matriculas de un albaran (se normalizan a mayusculas)
curl -s -X PUT "http://localhost:5010/albaranes/ALB000123" \
  -H "Content-Type: application/json" \
  -d '{"matricula_tractor":"1234BCD","matricula_remolque":"R5678BCD","cambio_matricula_tractor":"","cambio_matricula_remolque":""}'

# 3) Generar y subir el DeCA (respuesta: {"url":"https://denox.eu/DECA/deca_storage/DECA_ALB000123_ab12cd34ef56.pdf", ...})
curl -s -X POST "http://localhost:5010/albaranes/ALB000123/deca"

# 4) Enviar el DeCA al conductor
curl -s -X POST "http://localhost:5010/albaranes/ALB000123/deca/email" \
  -H "Content-Type: application/json" -d '{"email":"conductor@example.com"}'
```

## 7. Flujo en la interfaz

1. La pantalla carga los albaranes (`GET /albaranes`). Los datos del albaran son de solo lectura.
2. El usuario escribe las 4 matriculas; al salir del campo se guardan automaticamente (`PUT`).
3. Con el boton **Generar DeCA** (por fila o por seleccion multiple) la app:
   recupera los datos de SQL Server, genera el PDF nativo con QR, lo sube al endpoint
   HTTPS, guarda la URL en `ZDECAURL_0` y marca el albaran como generado (`ZDECAEST_0 = 1`).
4. Si el albaran ya esta generado, se ofrece **Descargar** y **Enviar email**.

## 8. QR y URL de descarga (paso necesario en el PHP)

El QR del PDF debe contener la URL final, que solo se conoce cuando el servidor decide el
nombre del fichero. Para resolverlo de forma **retrocompatible**, `upload_deca.php` acepta
ahora un campo opcional `filename`:

- Si llega `filename` (lo envia la app), se sanea con `basename()` y se valida con
  `^[A-Za-z0-9._-]+\.pdf$`; se usa como nombre definitivo. Asi la URL incrustada en el QR
  coincide con la URL almacenada en la base de datos.
- Si no llega (clientes antiguos), se mantiene el comportamiento original: nombre aleatorio.

## 9. Garantias tecnicas

- **PDF nativo**: ReportLab genera texto vectorial (no escaneado ni imagen de documento).
- **Metadatos**: ReportLab escribe `CreationDate` y `ModDate` en el PDF.
- **Limite de 5 MB**: se comprueba antes de subir (`pdf_max_bytes`).
- **TLS >= 1.2**: contexto SSL explicito con `minimum_version = TLSv1_2` y verificacion de
  certificado; la URL de subida debe empezar por `https://`.
- **URL de descarga directa**: se rechazan URLs con credenciales incrustadas.
- **Validacion de matriculas**: `1234BCD` (actual, 4 digitos + 3 letras: 1234BCD, 1234ABC), `R1234BCD` (remolque) y `M1234AB` (antigua).
- **Errores**: SQL Server inaccesible -> HTTP 503; fallo de subida -> HTTP 502 (el albaran
  no se marca como generado y puede reintentarse); todos los errores quedan en los logs.

## 10. Resolucion de incidencias

| Sintoma | Causa probable / solucion |
|---|---|
| `503 / HYT00 Login timeout expired` | Tiempo de espera agotado al conectar: revisar VPN/firewall/puerto y aumentar `DB_TIMEOUT`. Ejecutar `python test_db_connection.py` para ver el punto exacto de fallo. |
| `502` al generar | El endpoint devolvio error (dominio inaccesible, limite 5 MB, MIME). Ver `docker compose logs -f deca-app`. |
| El QR abre un PDF sin QR | El PHP no esta aplicando el campo `filename` (comprobar la version desplegada). |
| `Error al guardar en SQL Server` | La tabla `LIVE.ZDECA` no existe o el usuario no tiene permisos: ejecutar `sql/create_zdeca.sql`. |
| Envio por email no configurado | Rellenar las variables `SMTP_*` en `.env` y recrear el contenedor. |
---

## 11. Cumplimiento de la Resolucion de 5 de junio de 2026 (DeCA)

Basado en la propia Resolucion y en el webinar de la Subdireccion General de Inspeccion
de Transporte por Carretera y Ferrocarril (julio 2026):

| Apartado de la Resolucion | Como lo cubre la aplicacion |
|---|---|
| Primero. Aplicaciones informaticas | Los datos de la orden se leen de SQL Server (Sage X3) y se transforman en fichero electronico antes del inicio del servicio; se registran fechas de creacion/modificacion en `LIVE.ZDECA` (`CREDAT_0` / `UPDDAT_0`); cada fichero tiene una URL unica y especifica |
| Segundo. Documento electronico | PDF muy por debajo del limite de 5 MB (validado antes de subir), con metadatos de fecha/hora de creacion y modificacion (`CreationDate` / `ModDate`), nativo digital (texto vectorial, no escaneado) y con codigo QR que contiene la URL de descarga |
| Tercero. Direccion web (URL) | HTTPS obligatorio con TLS >= 1.2 y verificacion de certificado; una URL unica por documento; se rechazan URLs con credenciales o que requieran autenticacion (descarga directa) |
| Cuarto. Firmas (en su caso) | No se incluye firma por defecto (el DeCA no se usa aqui con finalidad contractual). Ampliable a firma avanzada eIDAS si se requiere |
| Quinto. Modificacion de datos | Al regenerar un DeCA se emite un PDF nuevo con **nueva URL y nuevo QR** (opcion 2 de la Resolucion); el PDF puede reenviarse al conductor desde la interfaz |
| Septimo. Copia del DeCA para el conductor | Descarga directa desde la interfaz (`GET /albaranes/{id}/deca/pdf`) o envio por email (`POST /albaranes/{id}/deca/email`), siempre con QR |
| Conservacion al menos 1 ano | El PDF queda en el repositorio corporativo (`https://denox.eu/DECA/deca_storage/`) y ademas en el volumen Docker persistente `deca_pdf` |

> Nota: la Resolucion permite desactivar la descarga de la URL a los siete dias naturales
tras finalizar el servicio. Si se necesita, puede anadirse un proceso de purga/desactivacion
sobre `deca_storage` sin afectar a los datos de `LIVE.ZDECA`.


## 12. Rendimiento del listado

El historico de albaranes es muy amplio (mas de 75.000 filas), por lo que el listado:

- Devuelve **200 filas por defecto** (`limite`, maximo `ALBARANES_LIMITE_MAX`) y permite
  paginar (`desplazamiento`) y buscar (`filtro`) por numero de albaran o destinatario.
- Completa la **naturaleza de la mercancia** en una segunda consulta ligera: hacerlo dentro
  de la consulta del listado multiplicaba el tiempo de respuesta (de 0,7 s a 14 s con 200
  filas) y podia saturar las conexiones de SQL Server (provocando `HYT00 Login timeout`).

Tiempos medidos sobre la base real: 200 filas en ~0,7 s y 500 filas en ~0,95 s.

## 13. Diagnostico rapido de la conexion

```bash
# Dentro del contenedor
docker compose exec deca-app python test_db_connection.py

# En local, con las dependencias instaladas
python test_db_connection.py
```

El script comprueba en orden: variables de entorno, conectividad TCP al puerto de la
instancia, login con credenciales, version del motor, total de albaranes, pendientes de
DeCA y DeCA ya generados. Devuelve codigo 0 si todo es correcto.


## 14. Docker en CentOS: configuracion_incompleta / env_localizado: false

La aplicacion ya NO depende de que exista el fichero .env dentro del contenedor:
la configuracion se inyecta como variables de entorno reales (via principal) y el
fichero es solo una comodidad adicional.

### Procedimiento en el servidor CentOS

```bash
cd /ruta/a/deca_app                 # carpeta que contiene docker-compose.yml
ls -l .env                          # 1) el fichero DEBE existir AQUI (en el host)
chmod 644 .env                      # 2) legible por el contenedor (uid 1001)
docker compose config | grep DB_    # 3) comprobar que compose LEE los valores
docker compose up -d --build --force-recreate   # 4) recrear (restart NO basta)
curl -s http://localhost:5010/health            # 5) verificar
```

El paso 3 es la comprobacion clave: si `docker compose config` muestra
`DB_SERVER: 192.168.28.100,51439`, las variables llegaran al contenedor. Si muestra
`DB_SERVER: ''`, compose no esta leyendo el .env: revisar que se ejecuta desde la
carpeta correcta y que el fichero se llama exactamente `.env`.

### Como interpretar /health

```json
{"estado":"ok", "archivo_env":"/app/.env", "env_localizado":false,
 "rutas_env_revisadas":{"/app/.env":false, "/.env":false, "/run/secrets/deca.env":false},
 "db_server_definido":true, "variables_ausentes":[]}
```

- Lo que importa es **estado: ok** y **variables_ausentes: []**. Con eso la app
  funciona, aunque `env_localizado` sea false (significa que la configuracion llega
  por variables de entorno y no por fichero, que es lo normal en Docker).
- `rutas_env_revisadas` indica todas las rutas donde se ha buscado el fichero y si
  existen: util para saber donde montarlo si se prefiere esa via.
- Si sale **configuracion_incompleta**, mira `variables_ausentes` y ejecuta el paso 3.

### Notas de CentOS/RHEL

- **SELinux**: el montaje del .env usa la etiqueta `:Z`. Si diera problemas, puede
  comentarse la linea `- ./.env:/app/.env:ro,Z` del compose: la aplicacion seguira
  funcionando con las variables de `environment`.
- **Otra ruta para el fichero**: la variable `DECA_ENV_FILE` permite indicar
  cualquier ruta, por ejemplo `-e DECA_ENV_FILE=/run/secrets/deca.env`.
- **Sin compose**: `docker run -d --env-file .env -p 5010:5010 deca-app`.
- **Diagnostico completo de la conexion**:
  `docker compose exec deca-app python test_db_connection.py`
