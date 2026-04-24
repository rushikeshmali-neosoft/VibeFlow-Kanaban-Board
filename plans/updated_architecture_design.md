# VibeFlow Kanban Board - Updated Architecture Design

## Overview
Based on analysis of existing implementation and documentation, this updated architecture addresses identified gaps while maintaining the modular monolithic Spring Boot + Angular design.

## Core Architecture Principles

1. **Modular Monolithic Architecture** - Spring Boot with clear module boundaries
2. **Clean Architecture Layers** - Controller → Service → Repository pattern
3. **Event-Driven Design** - Spring ApplicationEvents for internal communication
4. **Real-Time Collaboration** - WebSocket (STOMP) for live updates
5. **Immutable Audit Trail** - Assignment history and worklog immutability
6. **Shared Board Model** - Single global board visible to all authenticated users

## Technology Stack

### Backend (Spring Boot 3.2.4)
- **Java 17** - LTS version for stability
- **Spring Boot 3.2.4** - Latest stable release
- **PostgreSQL 15** - Primary database (consistent with pom.xml)
- **Spring Security** - JWT-based authentication
- **Spring Data JPA** - Data access layer
- **Spring WebSocket** - Real-time updates with STOMP
- **Spring Validation** - Input validation
- **Spring Actuator** - Health monitoring
- **SpringDoc OpenAPI** - API documentation
- **Lombok** - Reduced boilerplate code
- **ModelMapper** - Entity-DTO mapping

### Frontend (Angular 17+)
- **Angular 17+** - Latest stable version
- **Angular Material** - UI component library
- **Angular CDK Drag & Drop** - Kanban board interactions
- **RxJS** - Reactive programming
- **WebSocket (SockJS + STOMP)** - Real-time communication
- **SCSS** - Styling with CSS preprocessor

## Database Schema Updates

### Key Changes from Current Implementation:
1. **Consistent PostgreSQL usage** (not MySQL as shown in some docs)
2. **Proper indexing** for performance on large boards
3. **Cascade constraints** for data integrity
4. **Audit columns** on all entities

### Updated Entity Relationships:
```
User (1) ──┐ (created_by)
           ├── (assignee) ── Task (1) ──┐ (task)
           └── (changed_by) ── AssignmentHistory (N) ──┐ (task)
           └── (user) ── Worklog (N) ──┐ (task)

Task (1) ──┐ (task) ── AssignmentHistory (N)
           └── (task) ── Worklog (N)
```

## Backend Module Architecture

### 1. Auth Module (COMPLETE)
- **User Entity** - Email, password hash, timestamps
- **AuthService** - Registration, login, token generation
- **JwtTokenProvider** - JWT creation/validation
- **SecurityConfig** - Spring Security configuration
- **AuthController** - `/api/auth/*` endpoints

### 2. User Module (COMPLETE)
- **UserService** - User retrieval for assignment dropdown
- **UserController** - `/api/users` endpoint
- **UserMapper** - Entity ↔ DTO mapping

### 3. Task Module (COMPLETE)
- **Task Entity** - Title, status, position, assignee, createdBy, dueDate
- **TaskService** - CRUD operations, status updates, reordering
- **TaskController** - `/api/tasks/*` endpoints
- **TaskRepository** - Custom queries for position management
- **TaskMapper** - Entity ↔ DTO mapping

### 4. Board Module (COMPLETE)
- **BoardService** - Aggregates tasks by status columns
- **BoardController** - `/api/board` endpoint
- **BoardDTO** - Column structure + tasks

### 5. Assignment Module (COMPLETE)
- **AssignmentHistory Entity** - Old/new assignee, changed_by, timestamp
- **AssignmentService** - Assignment updates with history tracking
- **AssignmentController** - `/api/tasks/{id}/assignee` and `/api/tasks/{id}/history`
- **AssignmentChangedEvent** - Spring event for assignment changes
- **AssignmentHistoryEventListener** - Listener to save history

### 6. Worklog Module (COMPLETE)
- **Worklog Entity** - Immutable time logging (hours, user, task, timestamp)
- **WorklogService** - Time logging with validation
- **WorklogController** - `/api/tasks/{id}/worklogs` endpoints
- **WorklogMapper** - Entity ↔ DTO mapping

### 7. Report Module (COMPLETE)
- **ReportService** - Aggregates worklogs across tasks
- **ReportController** - `/api/reports/time` endpoint
- **TimeReportDTO** - Task summaries + grand total

### 8. WebSocket Module (COMPLETE)
- **WebSocketConfig** - STOMP configuration, `/ws` endpoint
- **WebSocketService** - Message broadcasting
- **TaskWebSocketEventListener** - Listens to events and broadcasts
- **WebSocketEvent** - DTO for real-time messages

### 9. Common Module (COMPLETE)
- **TaskStatus Enum** - 8 workflow columns
- **Exceptions** - GlobalExceptionHandler, ValidationException, NotFoundException
- **DTOs** - ApiResponse, ErrorResponse
- **OpenApiConfig** - Swagger/OpenAPI documentation

## Frontend Component Architecture

### 1. Core Services (COMPLETE)
- **AuthService** - Login, registration, token management
- **TaskService** - Task CRUD operations
- **UserService** - User retrieval for dropdowns
- **WebSocketService** - Real-time connection management
- **BoardStateService** - Centralized board state management
- **ReportService** - Time report data fetching

### 2. Authentication Components (COMPLETE)
- **Login Component** - User authentication
- **Register Component** - New user registration
- **Auth Guard** - Route protection
- **Auth Interceptor** - JWT token injection

### 3. Board Components (NEEDS ENHANCEMENT)
- **Board Component** - Main Kanban board (8 columns)
- **Task Card Component** - Individual task display (needs better styling)
- **Column Component** - Status column container (needs drag-drop improvements)

### 4. Task Management Components (NEEDS ENHANCEMENT)
- **Task Dialog Component** - Create/edit task modal (needs assignment dropdown, worklog form)
- **Assignment History Display** - Show assignment changes in modal
- **Worklog Form** - Time logging interface

### 5. Report Components (NEEDS ENHANCEMENT)
- **Time Report Component** - Report page at `/reports/time` (needs proper table styling)

### 6. Layout Components (COMPLETE)
- **Navbar Component** - Navigation with logout
- **App Component** - Root component with WebSocket integration

## API Endpoints Summary

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login (returns JWT)
- `GET /api/auth/me` - Current user info

### Users
- `GET /api/users` - All users (for assignment dropdown)

### Board
- `GET /api/board` - Full board with columns and tasks

### Tasks
- `POST /api/tasks` - Create new task
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task details
- `PATCH /api/tasks/{id}/status` - Update task status (drag between columns)
- `PATCH /api/tasks/{id}/reorder` - Reorder within column
- `DELETE /api/tasks/{id}` - Delete task (optional)

### Assignment
- `PATCH /api/tasks/{id}/assignee` - Assign/unassign task
- `GET /api/tasks/{id}/history` - Assignment history

### Worklogs
- `POST /api/tasks/{id}/worklogs` - Log time
- `GET /api/tasks/{id}/worklogs` - Get task worklogs

### Reports
- `GET /api/reports/time` - Time report with totals

## Real-Time Event Flow

### Event Types:
1. **TASK_CREATED** - New task added to backlog
2. **TASK_UPDATED** - Task status/position changed
3. **ASSIGNMENT_UPDATED** - Assignee changed

### Flow:
```
API Call → Service Method → Database Update → Publish Spring Event
→ Event Listener → WebSocket Broadcast → Frontend Update
```

### WebSocket Configuration:
- **Endpoint**: `/ws`
- **Subscribe**: `/topic/tasks`
- **Message Format**: `{ type: "EVENT_TYPE", data: {...} }`

## Transaction Management Strategy

### @Transactional Usage:
1. **Task Creation** - Create task with initial position
2. **Status Update** - Move between columns with position recalculation
3. **Assignment Update** - Update assignee + save history
4. **Worklog Creation** - Add immutable worklog

### Event Publishing:
- Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
- Ensures events only fire after successful transaction commit
- Prevents broadcasting of failed operations

## Validation Rules

### Task Validation:
- Title: Required, max 255 characters
- Status: Must be one of 8 predefined values
- Position: Positive integer

### Worklog Validation:
- Hours: Required, positive decimal (0.01 - 1000)
- User: Must be authenticated
- Task: Must exist

### Assignment Validation:
- Assignee: Must be existing user or null (unassign)
- Cannot assign to current assignee

## Performance Optimizations

### Backend:
1. **Database Indexing** - Index on `task(status, position)` for sorting
2. **Eager vs Lazy Loading** - Strategic `@EntityGraph` usage
3. **Batch Updates** - Position resequencing in batches
4. **Caching Strategy** - Consider Spring Cache for user list

### Frontend:
1. **Virtual Scrolling** - For columns with many tasks
2. **Optimistic Updates** - Immediate UI feedback for drag-drop
3. **WebSocket Throttling** - Buffer events to prevent UI jank
4. **Lazy Loading** - Report module loaded on demand

## Security Implementation

### Authentication:
- JWT tokens with 24-hour expiration
- Password hashing with bcrypt
- Stateless session management

### Authorization:
- All endpoints except `/api/auth/*` require authentication
- No role-based access (all authenticated users equal)
- Users can only modify their own worklogs (immutable anyway)

### Input Validation:
- Server-side validation for all inputs
- SQL injection prevention via JPA
- XSS protection via proper escaping

## Testing Strategy

### Backend Tests (>90% Coverage):
- **Unit Tests** - Service layer logic
- **Repository Tests** - `@DataJpaTest` with H2
- **Controller Tests** - `@WebMvcTest` with MockMvc
- **Integration Tests** - `@SpringBootTest` with Testcontainers
- **WebSocket Tests** - `@SpringBootTest` with TestMessageBroker

### Frontend Tests:
- **Component Tests** - Angular TestBed
- **Service Tests** - Mock HTTP and WebSocket
- **E2E Tests** - Cypress for critical user flows

### Test Coverage Requirements:
- Time logging logic
- Assignment history creation
- Time report aggregation
- Drag-drop position management

## Deployment Architecture

### Docker Compose Setup:
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: vibeflow
      POSTGRES_USER: vibeflow
      POSTGRES_PASSWORD: vibeflow
    
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vibeflow
    
  frontend:
    build: ./frontend
    ports:
      - "4200:80"
    depends_on:
      - backend
```

### Environment Configuration:
- **Development** - Local PostgreSQL, hot reload
- **Production** - Environment variables for secrets
- **Health Checks** - Spring Actuator endpoints

## Identified Gaps and Solutions

### Backend Gaps:
1. **✅ Database Consistency** - Use PostgreSQL as per pom.xml
2. **✅ Missing Event Listeners** - Implement AssignmentHistoryEventListener
3. **✅ WebSocket Integration** - Ensure events broadcast correctly
4. **✅ Test Coverage** - Add missing unit tests

### Frontend Gaps:
1. **UI Styling** - Match JIRA Kanban visual design
2. **Task Card Design** - Display assignee, due date, created by
3. **Assignment Dropdown** - User selection in task modal
4. **Worklog Interface** - "Log Work" button and form
5. **Assignment History Display** - Chronological list in modal
6. **Time Report UI** - Proper table with totals
7. **Drag-Drop UX** - Visual feedback during operations
8. **Responsive Design** - Mobile-friendly layout

## Implementation Priority

### Phase 1: Backend Completion (Week 1)
1. Verify all modules are fully implemented
2. Add missing unit tests to reach >90% coverage
3. Test WebSocket event flow end-to-end
4. Validate database schema and constraints

### Phase 2: Frontend Enhancement (Week 2)
1. Implement JIRA-like UI styling
2. Enhance task card component
3. Improve task dialog with assignment dropdown
4. Add worklog form to modal
5. Display assignment history

### Phase 3: Integration & Testing (Week 3)
1. End-to-end testing of all user flows
2. Performance testing with concurrent users
3. Security penetration testing
4. Documentation completion

## Success Metrics

1. **All Acceptance Criteria Met** - From PROJECT_DISCRIPTION.md
2. **Test Coverage >90%** - Unit and integration tests
3. **API Documentation Available** - Swagger UI at `/swagger-ui.html`
4. **Real-Time Updates <1s** - WebSocket latency
5. **Page Load <3s** - Initial board load time
6. **Concurrent Users >100** - Performance under load
7. **Zero Critical Security Issues** - Security audit passed

## Risk Mitigation

1. **Database Performance** - Index optimization and query tuning
2. **WebSocket Scalability** - Connection pooling and message batching
3. **Frontend Bundle Size** - Code splitting and lazy loading
4. **Browser Compatibility** - Test on Chrome, Firefox, Safari
5. **Mobile Responsiveness** - Progressive enhancement approach

This updated architecture maintains the project's core requirements while addressing identified gaps for a production-ready Kanban board application.