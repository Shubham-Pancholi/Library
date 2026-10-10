#!/usr/bin/env bash
set -euo pipefail

LIBRARY_PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd -- "$LIBRARY_PROJECT_DIR"

printf 'Project Directory: %s\n' "$PWD"

if [[ ! -f .env ]]; then
    printf 'Missing .env. Copy .env.example to .env and fill in local values.\n' >&2
    exit 1
fi

set -a
source ./.env
set +a

: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
: "${POSTGRES_USER:?Set POSTGRES_USER in .env}"
: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD in .env}"
: "${KEYCLOAK_ADMIN_USERNAME:?Set KEYCLOAK_ADMIN_USERNAME in .env}"
: "${KEYCLOAK_ADMIN_PASSWORD:?Set KEYCLOAK_ADMIN_PASSWORD in .env}"
: "${KEYCLOAK_DB_USERNAME:?Set KEYCLOAK_DB_USERNAME in .env}"
: "${KEYCLOAK_DB_PASSWORD:?Set KEYCLOAK_DB_PASSWORD in .env}"
: "${KEYCLOAK_STAFF_DEMO_PASSWORD:?Set KEYCLOAK_STAFF_DEMO_PASSWORD in .env}"
: "${KEYCLOAK_NONSTAFF_DEMO_PASSWORD:?Set KEYCLOAK_NONSTAFF_DEMO_PASSWORD in .env}"

printf 'Configuration validated.\n'

docker compose config --quiet

printf 'Starting PostgreSQL and Keycloak...\n'
docker compose up --wait --wait-timeout 120 postgres keycloak

printf 'Containers started.\n'

printf 'Waiting for Keycloak...\n'

for LIBRARY_ATTEMPT in {1..60}; do
    if curl -fs --max-time 2 \
        --output /dev/null \
        "http://localhost:8081/realms/library/.well-known/openid-configuration"; then
        printf 'Keycloak is ready.\n'
        break
    fi

    if [[ "$LIBRARY_ATTEMPT" -eq 60 ]]; then
        printf 'Keycloak did not become ready in time. Run docker compose logs keycloak\n' >&2
        exit 1
    fi

    sleep 2
done

printf 'Starting Spring Boot on http://localhost:8080...\n'
exec ./mvnw --no-transfer-progress spring-boot:run