# Despliegue con Docker — Cliente API Transporte (La Tacita 2.0)

## Cambio importante respecto a antes

Anteriormente el fichero `.env` con las credenciales se copiaba **dentro** de la imagen
(`COPY . .`), por eso bastaba con `docker run` sin pasar nada más.

Ahora, por seguridad, el `.env` queda **excluido** de la imagen mediante `.dockerignore`.
Por tanto, las credenciales deben **inyectarse al arrancar el contenedor** con la opción
`--env-file .env`.

> El fichero `.env` debe estar en el directorio desde el que ejecutas los comandos
> (la carpeta `cliente_api_transporte`).

> **IMPORTANTE — sin comillas en el `.env`:** Docker (`--env-file`) NO interpreta
> las comillas, a diferencia de `python-dotenv`. Si pones `FLASK_PORT="5002"`, el
> valor pasará a ser literalmente `"5002"` (con comillas) y verás errores como
> `'"5002"' is not a valid port number`. Escribe los valores **sin comillas**:
> `FLASK_PORT=5002`.


---

## 1. Construir (o reconstruir) la imagen

Tras los cambios en `Dockerfile`, `requirements.txt` y el código, hay que reconstruir:

```bash
sudo docker build -t transporte_py .
```

## 2. Eliminar el contenedor anterior (si existe)

Para poder reutilizar el nombre `apptrans`:

```bash
sudo docker rm -f apptrans
```

## 3. Lanzar el contenedor

La única diferencia respecto al comando anterior es añadir `--env-file .env`:

```bash
sudo docker run -d -p 5003:5002 --env-file .env --name apptrans transporte_py
```

- `-d` : en segundo plano (detached).
- `-p 5003:5002` : mapea el puerto 5003 del host al 5002 del contenedor.
- `--env-file .env` : inyecta las variables de entorno (credenciales y config).
- `--name apptrans` : nombre del contenedor.

La aplicación quedará accesible en: `http://IP_DEL_HOST:5003`

---

## Comandos útiles

```bash
# Ver logs
sudo docker logs -f apptrans

# Detener / arrancar
sudo docker stop apptrans
sudo docker start apptrans

# Estado
sudo docker ps -a | grep apptrans
```

---

## Notas

- La app se ejecuta ahora con **gunicorn** (servidor de producción) y como un
  **usuario sin privilegios** dentro del contenedor.
- `FLASK_DEBUG` está en `false` por defecto. No lo actives en producción.
- Si prefieres pasar variables sueltas en lugar del `--env-file`, puedes usar
  varias opciones `-e`, por ejemplo:
  `-e MYSQL_HOST=... -e MYSQL_PASSWORD=... -e DACHSER_API_KEY=...`
