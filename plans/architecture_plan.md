# VibeFlow Kanban Board - Architecture Plan

## Project Overview
A single-board, multi-user Kanban system with real-time collaboration features similar to JIRA Kanban board.

## Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.x.x**
- **MySQL** as primary database
- **Spring Security** with JWT authentication
- **Spring Data JPA** for data access
- **Spring WebSocket (STOMP)** for real-time updates
- **Spring Events** for event-driven architecture
- **Spring Actuator** for monitoring
- **SpringDoc OpenAPI** for API documentation
- **JUnit 5** & **Mockito** for testing
- **Maven** as build tool

### Frontend
- **Angular (latest)** - TypeScript framework
- **Angular Material** for UI components
- **RxJS** for reactive programming
- **Angular CDK Drag & Drop** for drag-and-drop functionality
- **Angular Router** for navigation
- **JWT authentication** with interceptors
- **WebSocket client** for real-time updates

### DevOps
- **Docker** for containerization
- **Docker Compose** for local development
- **MySQL** container

## System Architecture

### Modular Monolithic Design
```
com.vibeflow
├── auth/           # Authentication & authorization
├── user/           # User management
├── board/          # Board structure & columns
├── task/           # Task management
├── assignment/     # Assignment & history tracking
├── worklog/        # Time logging (immutable)
├── report/         # Reporting & aggregation
├── websocket/      # Real-time communication
└── common/         # Shared components
```

### Layered Architecture
```
Controller Layer (REST APIs)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (MySQL)
```

### Event-Driven Flow
```
API Call → Service → Database Update → Spring Event → 
    ↓
Listener (Assignment History) → WebSocket Broadcast
```

## Database Schema

### Core Entities
1. **User**
   - id, email, password_hash, created_at

2. **Task**
   - id, title (max 255), status, position, assignee_id, created_by, due_date, created_at, updated_at

3. **AssignmentHistory**
   - id, task_id, old_assignee_id, new_assignee_id, changed_by_id, changed_at

4. **Worklog**
   - id, task_id, user_id, hours (decimal), created_at (immutable)

### Enums
- **TaskStatus**: BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, TESTING, DONE, CANCELLED, CLOSED

## API Design

### Authentication APIs
- POST `/api/auth/register` - User registration
- POST `/api/auth/login` - JWT token generation
- GET `/api/auth/me` - Current user info

### User APIs
- GET `/api/users` - List all users (for assignment dropdown)

### Board APIs
- GET `/api/board` - Get board structure with tasks

### Task APIs
- POST `/api/tasks` - Create new task
- GET `/api/tasks` - Get all tasks
- PATCH `/api/tasks/{id}/status` - Update status & position
- PATCH `/api/tasks/{id}/reorder` - Reorder within column

### Assignment APIs
- PATCH `/api/tasks/{id}/assignee` - Assign/unassign task
- GET `/api/tasks/{id}/assignment-history` - Get assignment history

### Worklog APIs
- POST `/api/tasks/{id}/worklogs` - Log time (decimal hours)
- GET `/api/tasks/{id}/worklogs` - Get worklogs for task

### Report APIs
- GET `/api/reports/time` - Time report with aggregation

## Real-Time Communication

### WebSocket Configuration
- Endpoint: `/ws`
- Topic: `/topic/tasks`
- Event Types:
  - `TASK_CREATED`
  - `TASK_UPDATED`
  - `ASSIGNMENT_UPDATED`

### Event Flow
1. User action triggers API call
2. Service updates database
3. Spring ApplicationEvent published
4. Event listener processes (e.g., saves history)
5. WebSocket broadcasts to all connected clients

## Key Features Implementation

### 1. Authentication & Security
- JWT-based stateless authentication
- BCrypt password hashing
- Protected endpoints (except auth APIs)
- Session persistence via token storage

### 2. Shared Board
- Single global board for all authenticated users
- Fixed 8 workflow columns
- Real-time updates via WebSocket
- All users see same tasks

### 3. Task Management
- Title validation (max 255 characters)
- Default status: BACKLOG
- Position tracking for drag-and-drop
- Created by logged-in user

### 4. Drag & Drop
- Column-to-column movement (status change)
- Within-column reordering (position update)
- Persistence after page refresh
- Real-time sync across users

### 5. Assignment Management
- Dropdown with all registered users
- Assignment history tracking
- Unassign option (null assignee)
- Chronological history (latest first)

### 6. Time Logging
- Decimal hours input
- Immutable worklogs (no edit/delete)
- Multiple worklogs per task
- Linked to logged-in user

### 7. Reporting
- Time report at `/reports/time`
- Per-task: title, status, assignee, total hours
- Grand total across all tasks
- Accessible to all logged-in users

## Validation Rules
1. Task title: max 255 characters
2. Hours: positive decimal
3. Assignee: must exist in database
4. Status: must be valid enum value
5. Position: non-negative integer

## Transaction Management
- `@Transactional` for:
  - Task creation
  - Assignment updates
  - Worklog creation
  - Status updates
- Events triggered AFTER commit

## Testing Strategy
- Unit tests for:
  - Time logging logic
  - Assignment history creation
  - Time report aggregation
- Integration tests for APIs
- Test coverage >90%
- TDD approach

## Deployment
- Docker containerization
- MySQL container
- Environment-based configuration
- Health checks via Actuator
- API documentation via Swagger UI

## Frontend Architecture

### Component Structure
```
src/app/
├── auth/              # Login, register components
├── board/             # Kanban board component
├── task/              # Task creation, details
├── report/            # Time report view
├── shared/            # Shared components
├── services/          # API services
├── guards/            # Route guards
└── interceptors/      # HTTP interceptors
```

### Key Frontend Features
1. Responsive Kanban board with 8 columns
2. Drag-and-drop with Angular CDK
3. Task modal with assignment dropdown
4. Time logging form
5. Real-time updates via WebSocket
6. JWT authentication flow
7. Report view with table

## Development Workflow
1. Backend implementation first (Spring Boot)
2. Frontend implementation (Angular)
3. Integration testing
4. End-to-end validation
5. Docker setup
6. Documentation

## Constraints & Boundaries
- No RBAC or admin roles
- No multiple boards/projects
- No file attachments or comments
- No custom workflow columns
- No email/push notifications
- No offline support
- No AI features
- Monolithic only (no microservices)