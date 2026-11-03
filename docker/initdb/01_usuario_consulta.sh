#!/bin/bash
# Se ejecuta SOLO la primera vez que se crea el volumen de PostgreSQL.
# Crea el usuario "consulta" de solo lectura para DBeaver u otros clientes: puede leer todo y no puede modificar nada.
set -e

if [ -z "$DB_CONSULTA_PASSWORD" ]; then
  echo "[initdb] DB_CONSULTA_PASSWORD vacío: no se crea el usuario de solo lectura 'consulta'."
  exit 0
fi

psql -v ON_ERROR_STOP=1 -v clave="$DB_CONSULTA_PASSWORD" -v base="$POSTGRES_DB" \
     --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-'EOSQL'
  CREATE ROLE consulta LOGIN PASSWORD :'clave';
  GRANT CONNECT ON DATABASE :"base" TO consulta;
  GRANT pg_read_all_data TO consulta;
EOSQL
echo "[initdb] Usuario de solo lectura 'consulta' creado."
