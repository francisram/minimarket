# Instructivo: pasos posteriores a levantar el sistema

Se hace **una sola vez**, después de `docker compose up -d --build` y de haber cambiado las
contraseñas temporales de `admin` y `owner` desde la pantalla de login.

## 1. Quitar las contraseñas temporales del `.env`

`APP_SECURITY_DEFAULT_ADMIN_PASSWORD` y `APP_SECURITY_DEFAULT_OWNER_PASSWORD` solo se usan al **crear**
los usuarios `admin` y `owner` la primera vez. Una vez creados la API las ignora, y el
`docker-compose.yml` las declara con valor vacío por defecto: borrarlas no rompe nada ni requiere reiniciar.

El `.env` está en el servidor (host), no dentro de ningún contenedor.

### a) Verificar que ambos usuarios ya cambiaron la contraseña

```bash
docker exec heladeria-postgres psql -U postgres -d heladeriadb \
  -c "select username, debe_cambiar_password from usuarios where username in ('admin','owner')"
```

Ambas filas deben mostrar `f` (falso). Si alguna muestra `t`, ese usuario todavía no cambió la clave:
no borres su línea todavía.

### b) Borrar las dos líneas

```bash
cd /ruta/al/proyecto/minimarket
sed -i '/^APP_SECURITY_DEFAULT_ADMIN_PASSWORD=/d;/^APP_SECURITY_DEFAULT_OWNER_PASSWORD=/d' .env
grep -c "DEFAULT_.*_PASSWORD" .env   # debe imprimir 0
```

Conviene dejar el `.env` con permisos restringidos: `chmod 600 .env`.

## 2. Conectarse con DBeaver (solo lectura)

Usuario `consulta` y la clave `DB_CONSULTA_PASSWORD` del `.env`. El puerto de PostgreSQL solo escucha en
`127.0.0.1:5435` del servidor: desde otra máquina se usa un acceso remoto al servidor (AnyDesk, etc.) y
DBeaver instalado allí.

Si el volumen de PostgreSQL es anterior al script `docker/initdb/01_usuario_consulta.sh`, el rol no existe
(ese script solo corre cuando el volumen se crea de cero). En ese caso se crea a mano:

```bash
set -a; . ./.env; set +a
docker exec -e P="$DB_CONSULTA_PASSWORD" heladeria-postgres bash -c \
  "psql -v clave=\"\$P\" -U postgres -d heladeriadb -c \"CREATE ROLE consulta LOGIN PASSWORD :'clave'\" \
   -c 'GRANT CONNECT ON DATABASE heladeriadb TO consulta' -c 'GRANT pg_read_all_data TO consulta'"
```

## 3. Si la API no arranca con "password authentication failed for user postgres"

`POSTGRES_PASSWORD` del `.env` solo se aplica cuando el volumen se crea. Si el `.env` cambió después, la base
conserva la clave vieja. Para alinearla (sin perder datos):

```bash
set -a; . ./.env; set +a
docker exec heladeria-postgres psql -U postgres -c "ALTER USER postgres PASSWORD '$POSTGRES_PASSWORD'"
docker restart heladeria-api
```
