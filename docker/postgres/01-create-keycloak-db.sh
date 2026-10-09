#!/usr/bin/env bash
set -euo pipefail

psql \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    --set=ON_ERROR_STOP=1 \
    --set=keycloak_user="$KEYCLOAK_DB_USERNAME" \
    --set=keycloak_password="$KEYCLOAK_DB_PASSWORD" <<'SQL'
CREATE ROLE :"keycloak_user" LOGIN PASSWORD :'keycloak_password';
CREATE DATABASE keycloak OWNER :"keycloak_user";
SQL