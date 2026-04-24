# VibeFlow Kanban Board

VibeFlow is a single-board Kanban system inspired by Jira Kanban. It includes:

- JWT-based authentication
- Shared 8-column board for all authenticated users
- Drag and drop status changes and reordering
- Assignment updates with assignment history
- Immutable worklogs with decimal-hour logging
- Time report aggregation
- WebSocket task updates
- Swagger API documentation

## Tech Stack

- Backend: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, WebSocket, Swagger
- Frontend: Angular 21, Angular Material, Angular CDK, RxJS
- Database: PostgreSQL 16

## Run With Docker

From the project root:

```bash
docker compose up --build
```

After startup:

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator Health: `http://localhost:8080/actuator/health`

## Run Locally

### Backend

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

Default local database settings:

- Host: `localhost`
- Port: `5432`
- Database: `vibeflow_db`
- Username: `vibeflow`
- Password: `vibeflow`

Environment overrides:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`

If startup fails:

- `Connection to localhost:5432 refused`: ensure Postgres is running with `docker compose up -d postgres`.
- `Port 8080 was already in use`: stop the existing process, for example in PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object OwningProcess
Stop-Process -Id <PID> -Force
```

### Frontend

```bash
cd frontend
npm install
npm start
```

The frontend expects the backend at `http://localhost:8080`.

## Tests

Backend tests:

```bash
cd backend
mvn test
```

Frontend production build:

```bash
cd frontend
npm run build
```

## API Overview

All protected endpoints require `Authorization: Bearer <token>`.

### Authentication

`POST /api/auth/register`

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

`POST /api/auth/login`

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

`POST /api/auth/logout`

`GET /api/auth/me`

### Users

`GET /api/users`

`GET /api/users/{id}`

### Board

`GET /api/board`

Example response:

```json
{
  "columns": [
    "BACKLOG",
    "TODO",
    "IN_PROGRESS",
    "IN_REVIEW",
    "TESTING",
    "DONE",
    "CANCELLED",
    "CLOSED"
  ],
  "tasks": []
}
```

### Tasks

`POST /api/tasks`

```json
{
  "title": "Implement login flow",
  "dueDate": "2026-05-01"
}
```

`GET /api/tasks`

`GET /api/tasks/{id}`

`PATCH /api/tasks/{id}/status`

```json
{
  "status": "IN_PROGRESS",
  "position": 2
}
```

`PATCH /api/tasks/{id}/reorder`

```json
{
  "position": 1
}
```

### Assignment

`PATCH /api/tasks/{id}/assignee`

```json
{
  "assigneeId": 2
}
```

To unassign:

```json
{
  "assigneeId": null
}
```

`GET /api/tasks/{id}/assignment-history`

### Worklogs

`POST /api/tasks/{id}/worklogs`

```json
{
  "hours": 2.5
}
```

`GET /api/tasks/{id}/worklogs`

### Reports

`GET /api/reports/time`

Example response:

```json
{
  "tasks": [
    {
      "taskId": 1,
      "title": "Implement login flow",
      "status": "DONE",
      "assignee": "user@example.com",
      "totalHours": 5.5
    }
  ],
  "grandTotal": 5.5
}
```

## WebSocket

- Endpoint: `/ws`
- Topic: `/topic/tasks`

Event types:

- `TASK_CREATED`
- `TASK_UPDATED`
- `ASSIGNMENT_UPDATED`
