# SAKA QMS CRUD API

Spring Boot REST API for the `qms_checklist` PostgreSQL schema.

## Run

1. Create the `qms_checklist` schema and tables using `commands.sql`.
2. Configure the database with environment variables if needed:

```text
DB_URL=jdbc:postgresql://localhost:5432/SAKA
DB_USERNAME=postgres
DB_PASSWORD=admin
DB_SCHEMA=qms_checklist
```

3. Start the application:

```bash
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

## Endpoints

Each resource supports `GET`, `GET /{id}`, `POST`, `PUT /{id}`, and `DELETE /{id}`:

| Table | Endpoint |
|---|---|
| departments | `/api/departments` |
| employees | `/api/employees` |
| name_of_item | `/api/items` |
| details | `/api/details` |

IDs are supplied by the client because the existing PostgreSQL tables define integer primary keys without identity generators.

Example department:

```json
{
  "id": 7,
  "deptName": "Quality",
  "manager": "Jane Doe",
  "modifiedBy": "admin",
  "modifiedOn": "2026-09-13T00:00:00",
  "authorisedBy": "admin"
}
```
