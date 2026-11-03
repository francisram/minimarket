# Instructivo: instalación en un equipo nuevo y pasos posteriores

## 0. Instalar en otro equipo (base limpia)

Con un volumen de PostgreSQL nuevo el sistema arranca **sin datos de prueba**. Solo crea lo imprescindible:

| Se crea | Detalle |
|---|---|
| Usuarios | `admin` y `owner`, con contraseña temporal que hay que cambiar al ingresar |
| Roles y páginas | ADMIN, OWNER y CAJERO, con las páginas del sistema |
| Planes y licencia | los planes de licencia y la licencia inicial |
| Categorías | la estructura de categorías de artículos |
| Institución | nombre genérico, sin RUC, timbrado, dirección ni teléfono |
| Caja 1 | un punto de expedición `001-001`, **sin timbrado** |
| Cliente | solo "Consumidor Final" |

No se crean sabores, presentaciones, toppings, productos, proveedores, compras, ventas ni una segunda caja.

Pasos:

1. Copiar los dos repositorios al equipo nuevo y crear `minimarket/.env` a partir de `.env.example`
   (`APP_JWT_SECRET`, `POSTGRES_PASSWORD`, `DB_CONSULTA_PASSWORD`, `APP_CIFRADO_CLAVE`; claves distintas a las de otros equipos).
2. En `minimarket/`: `docker compose up -d --build`. En `minimarketFront/`: `docker compose up -d --build`.
3. Ingresar como `admin` / `owner` con la contraseña temporal del `.env` (o la que muestra el log de la API) y cambiarla.
4. Cargar los datos reales: **Datos del negocio** (RUC, dirección, teléfono, timbrado de facturas) y, si hay más cajas,
   **Puntos de expedición**. Sin timbrado el sistema no deja facturar. El timbrado de notas de crédito se carga desde
   el botón "Nota de crédito" de una factura.
5. Cargar el catálogo real (sabores, presentaciones, productos, proveedores) y crear los usuarios del personal.
6. Impresoras, y comprobar que el respaldo automático de las 03:00 genera su primer archivo.

Para un equipo de **pruebas** con catálogo de ejemplo: `APP_SEED_DATOS_DEMO=true` en el `.env` antes del primer arranque.

Para llevar los datos de otro equipo en vez de empezar de cero: restaurar un respaldo (`.dump`) desde la página de
copias de seguridad; eso trae todo lo que tenía ese equipo, incluidos sus datos de prueba si los hubiera.


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
