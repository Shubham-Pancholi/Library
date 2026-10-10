# Library

A library project that is more than a simple CRUD.

## Requirements

- **Java 21 JDK**
- **Docker with Docker Compose (Docker must be running)**
- **Bash**
- **curl**

## Setup and Start

Run these commands from the project root directory where `pom.xml` is located.
Configure environment variables:

```bash
cp .env.example .env
```

> **Note:** Replace every placeholder in `.env` with local values.

Validate Configuration and Start Application:

```bash
./scripts/dev.sh
```

The script starts PostgreSQL and Keycloak, waits for PostgreSQL's health check and Keycloak's realm endpoint, then starts the Spring Application.

Endpoints:
- Application: http://localhost:8080
- Keycloak: http://localhost:8081
- PostgreSQL: localhost:5432

Smoke check: run this smoke check in a second terminal while Spring app is running.

```bash
curl -i http://localhost:8080/api/me
```

Should return a 401 - Unauthorized for a lack of token in the request.

## Demo Accounts

Realm: library
client: library-cli

| Username | Staff access | Password variable |
| --- | --- | --- |
| staff-demo | Yes | KEYCLOAK_STAFF_DEMO_PASSWORD |
| nonstaff-demo | No | KEYCLOAK_NONSTAFF_DEMO_PASSWORD |

> **Note:** Keycloak's startup import skips existing realms. Modifying the import file or demo password variables will not update the existing users.

## Stopping the Application

- To stop the Spring app (From the terminal where the application was run):
`Ctrl + C`
- To stop the Docker containers:

```bash
docker compose down
```

Named database volumes are preserved even after turning off the containers. The data remains available when the containers are started again.

## Run Tests

Run the test with Docker running, because the tests use Testcontainers:

```bash
./mvnw --batch-mode --no-transfer-progress verify
```