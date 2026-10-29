# TaskForge — Project Memory and Build Specification

> **Purpose:** Persistent project handover/memory file for future ChatGPT conversations. Keep this file at the project root and update it whenever the scope, architecture, files, configuration, endpoints, dependencies, or implementation status changes.

## 1. Project Identity

- **Project:** TaskForge
- **Type:** Enterprise-style Project Management SaaS
- **Goal:** Build a resume-worthy Java Full Stack application first, then learn its implementation step by step.
- **IDE / OS:** Eclipse IDE on Windows
- **Project root:** `D:\Seven Mentor\taskforge`
- **Java package root:** `com.taskforge`
- **Build tool:** Maven
- **Java:** 21
- **Spring Boot:** 3.5.16
- **Database:** PostgreSQL, database `taskforge_db`
- **Backend port:** 8080

## 2. Instructions for Future Sessions

1. User wants code, files, and exact implementation instructions first; detailed learning comes after the main project is built.
2. Provide complete code for every new or changed file, with its exact path/package and Eclipse steps.
3. Include testing instructions, URLs, HTTP methods, sample bodies, and expected status/results.
4. Do not repeat setup already completed unless debugging requires it.
5. Keep Java 21 and Spring Boot 3.5.16 unless the user approves a change. Do not switch to Spring Boot 4.
6. Keep a layered architecture and use DTOs at API boundaries.
7. Update this file after every meaningful project change. Preserve project history, update statuses/file inventory/endpoints/dependencies, and append a dated changelog entry.
8. The assistant cannot directly edit the user's local project folder. Provide a downloadable updated `chat.md` and tell the user to replace the project-root copy.
9. Never store actual passwords, JWT secrets, API keys, or other credentials here. Use environment-variable placeholders.
10. Do not mark a feature as complete until the user confirms it works or there is test evidence.
11. Prioritize secure defaults: validation, consistent errors, password hashing, authorization, tenant isolation, and automated tests.
12. The current work is implementation-first. Do not stop to teach every line unless the user asks; detailed explanations will follow after the build.

## 3. Product Scope

TaskForge is an enterprise-style multi-tenant project/task management application.

Planned features:
- User registration and login
- Organizations/workspaces and membership
- Role-based access control
- Projects belonging to organizations
- Tasks with status, priority, assignment, due dates, and descriptions
- Search/filter/sort/pagination
- Kanban workflow
- Request validation and consistent errors
- Spring Security, JWT, password hashing
- Tenant isolation
- Audit history and notifications
- React frontend
- PostgreSQL persistence
- JUnit 5 and Mockito tests
- Docker / Docker Compose
- GitHub Actions CI
- API documentation and deployment guide
- Professional README and resume-ready project summary

Build the backend first, then frontend, then production readiness. Keep the app runnable between milestones.

## 4. Planned Technology Stack

### Backend
- Java 21, Spring Boot 3.5.16, Maven
- Spring Web
- Spring Data JPA / Hibernate
- Jakarta Bean Validation
- PostgreSQL JDBC driver
- Spring Security
- JWT library (choose and record compatible version when implementing)
- JUnit 5, Mockito, Spring Boot Test
- OpenAPI/Swagger, after core API is stable

### Frontend
- React
- JavaScript or TypeScript (decide before frontend setup)
- React Router
- Fetch or Axios
- Responsive interface and Kanban board

### Tooling
- Git/GitHub, Postman, Docker, GitHub Actions

## 5. Environment and Setup Status

Previously completed and tested:
- JDK 21 installed/configured in Eclipse.
- Maven Spring Boot project created/imported.
- Spring Boot version set to 3.5.16.
- PostgreSQL installed and running.
- `taskforge_db` exists and connects successfully.
- Spring Boot app started on port 8080.
- Hibernate/JPA initialized and Hikari connected to PostgreSQL.
- Project CRUD API was tested successfully in Postman before the current refactor.

### `application.properties` configuration shape

Expected configuration (inspect actual local file before changing it):

```properties
spring.application.name=TaskForge
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/taskforge_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true
```

The user's local file may still contain a literal password. Do not ask them to paste it into chat. The safer target is an environment variable such as `DB_PASSWORD`.

## 6. Maven Dependencies

Previously reported dependencies in `pom.xml`:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `org.postgresql:postgresql` with runtime scope
- `spring-boot-starter-test` with test scope
- `spring-boot-maven-plugin`

Java 21 and Spring Boot parent 3.5.16. Inspect the real `pom.xml` if exact versions or setup are in doubt. No extra dependency should be needed for the current validation/DTO/exception milestone.

## 7. Current Implementation Inventory

### Existing files before current milestone

#### `src/main/java/com/taskforge/TaskforgeApplication.java`
- Main Spring Boot application class.
- Package `com.taskforge`.
- App startup previously succeeded.

#### `src/main/java/com/taskforge/entity/Project.java`
- JPA entity mapped to `projects`.
- Fields: `id`, `name`, `description`, `createdAt`.
- Uses `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@PrePersist`.
- Has no-argument constructor and a constructor accepting name and description.
- Getters for id/name/description/createdAt and setters for name/description.

#### `src/main/java/com/taskforge/repository/ProjectRepository.java`
- Extends `JpaRepository<Project, Long>`.

#### `src/main/java/com/taskforge/service/ProjectService.java`
- Previously implemented create/list/get-by-ID/update/delete.
- Constructor-injects `ProjectRepository`.
- Previously threw generic `RuntimeException` when ID was missing.
- Current milestone replaces it with DTO-based operations and a custom exception.

#### `src/main/java/com/taskforge/controller/ProjectController.java`
- Base route `/api/projects`.
- Previously supported POST, GET all, GET by ID, PUT, DELETE.
- Current milestone refactors it to use request/response DTOs and `@Valid`.

## 8. Current API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/projects` | Create a project |
| GET | `/api/projects` | List projects |
| GET | `/api/projects/{id}` | Get one project |
| PUT | `/api/projects/{id}` | Update a project |
| DELETE | `/api/projects/{id}` | Delete a project |

Expected request body for POST/PUT:

```json
{
  "name": "TaskForge Development",
  "description": "Building our enterprise project management platform"
}
```

Do not assume IDs start at 1; use an ID returned by POST or GET. Existing records remain in the database.

## 9. Planned Package Structure

```text
taskforge/
├── chat.md
├── README.md
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/taskforge/
│   │   │   ├── TaskforgeApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/application.properties
│   └── test/java/com/taskforge/
└── frontend/   # React application to be added later
```

This is the target direction, not a claim that all planned folders/files already exist.

## 10. Milestones and Status

Status: `[x]` complete; `[~]` implementation started/awaiting verification; `[ ]` not started.

### Milestone 0 — Environment and project skeleton
- [x] Java 21 configured
- [x] Spring Boot 3.5.16 Maven project
- [x] PostgreSQL connection
- [x] App starts successfully

### Milestone 1 — Project CRUD
- [x] Project entity
- [x] Project repository
- [x] Project service
- [x] Project controller
- [x] User confirmed CRUD API works in Postman

### Milestone 2 — API foundation and error handling
- [x] Request DTO and validation
- [x] Response DTO
- [x] Custom not-found exception
- [x] Global exception handler
- [x] Application compiled and started after refactor
- [x] User confirmed all Postman tests work
- [x] Valid CRUD requests work with DTOs
- [x] Blank project name returns HTTP 400
- [x] Malformed JSON returns HTTP 400
- [x] Unknown project ID returns HTTP 404

### Milestone 3 — Organization and tenant model
- [x] Organization CRUD implemented and user confirmed all tests work
- [x] User entity/repository and organization membership/role model — user confirmed startup works
- [x] Membership and role model verification
- [~] Project belongs to organization — implementation provided, pending compile and Postman verification
- [~] Project-organization relationship and repository queries — implementation provided, pending verification
- [ ] Strict authenticated tenant-scoped access (requires authentication/authorization)

### Milestone 4 — Authentication and authorization
- [ ] Spring Security
- [ ] Registration/login DTOs
- [ ] Password hashing
- [ ] JWT creation/validation
- [ ] Role-based authorization
- [ ] Tenant-isolation tests

### Milestone 5 — Tasks and workflow
- [ ] Task entity and relationships
- [ ] Task CRUD
- [ ] Status/priority enums
- [ ] Assignment and due dates
- [ ] Filtering/sorting/pagination
- [ ] Kanban-ready API

### Milestone 6 — Auditing and notifications
- [ ] Audit history
- [ ] Notification model/delivery approach
- [ ] Track important actions
- [ ] Tests

### Milestone 7 — Testing and API documentation
- [ ] Service unit tests
- [ ] Controller/API tests
- [ ] Repository/integration tests where practical
- [ ] OpenAPI/Swagger
- [ ] API examples and error cases

### Milestone 8 — React frontend
- [ ] Create React app in `frontend/`
- [ ] Login/register
- [ ] Organization/workspace view
- [ ] Projects view
- [ ] Task list/Kanban board
- [ ] Form validation/error handling
- [ ] Responsive styling
- [ ] Backend integration

### Milestone 9 — Production readiness
- [ ] Externalize secrets/config
- [ ] Dockerfile(s) and Docker Compose
- [ ] GitHub Actions build/test pipeline
- [ ] Logging and production profile
- [ ] Deployment instructions
- [ ] Complete README and resume summary
- [ ] End-to-end verification

## 11. Architecture and Security Decisions

- Layered architecture: Controller → Service → Repository → Database.
- Use DTOs at API boundaries; do not expose JPA entities as request bodies.
- Keep business logic in services.
- Use constructor injection.
- Return appropriate HTTP status codes and consistent error responses.
- Enforce organization membership and tenant filtering for all organization-owned resources.
- Never trust a client-supplied organization ID without checking authenticated user's membership.
- Never log secrets, passwords, JWTs, or sensitive request data.
- Consider Flyway/Liquibase when transitioning away from development-time `ddl-auto=update`.
- Record additional decisions here as they are made.

## 12. Current Next Action

Milestone 2, Organization CRUD, and the foundational user/membership model are confirmed working by the user. The current implementation step associates projects with organizations.
1. Update `entity/Project.java` with a `ManyToOne` relationship to `Organization`.
2. Add required `organizationId` to `dto/ProjectRequest.java` and include it in `ProjectResponse.java`.
3. Add organization-specific repository queries.
4. Update `ProjectService.java` to validate the organization exists, assign it on create/update, and list projects by organization.
5. Add `OrganizationProjectController.java` with GET `/api/organizations/{organizationId}/projects`.
6. User must compile, start, and test the new endpoints before this milestone is marked complete. Existing projects may retain a null organization ID until migrated; the relationship is nullable temporarily to preserve existing records. Strict tenant isolation still requires authentication and authorization later.

### Current organization endpoints (verified by user)
- POST `/api/organizations` — create organization
- GET `/api/organizations` — list organizations
- GET `/api/organizations/{id}` — get one organization
- PUT `/api/organizations/{id}` — update organization
- DELETE `/api/organizations/{id}` — delete organization

### User and membership model (verified by user)
- No new public endpoints yet.
- `AppUser` maps to `app_users`; stores display name, unique email, password hash, and creation timestamp.
- `OrganizationRole` enum: `OWNER`, `ADMIN`, `MEMBER`.
- `OrganizationMembership` maps to `organization_memberships`; many-to-one links to app user and organization, with a unique constraint on `(organization_id, user_id)`.
- Repositories support email lookup and membership queries.
- User confirmed the application starts successfully with these model classes.
- Password hashing and authentication are not implemented yet.

### Project-organization association (implementation provided; pending verification)
- `Project` now has a `ManyToOne` relationship to `Organization` using `organization_id`. The column is nullable temporarily to preserve existing project rows.
- `ProjectRequest` now requires `organizationId`; `ProjectResponse` includes it.
- `ProjectRepository` adds queries for projects by organization and project membership checks.
- `ProjectService` validates the organization on create/update and supports listing projects for one organization.
- New controller route: GET `/api/organizations/{organizationId}/projects`.
- Existing `/api/projects` routes remain. No strict tenant authorization is implemented yet.

## 13. Change Log

### 2026-10-27 — Project memory file created
- Consolidated known project context, environment, current implementation, roadmap, and workflow preferences.
- Recorded implementation-first workflow and planned API validation milestone.
- No application source code changed in that event.

### 2026-10-27 — Milestone 2 implementation started
- Began refactoring the Project CRUD API to use request/response DTOs.
- Planned centralized validation and error handling.
- Completion is pending local compilation and Postman verification; do not mark the milestone complete until tested.


### 2026-10-27 — Milestone 2 verified
- User confirmed the validation/DTO/global exception handling implementation works.
- Marked Milestone 2 complete.
- Next milestone: Organization CRUD API, then user/membership relationships and tenant isolation.


### 2026-10-27 — Organization CRUD code provided
- Added implementation instructions/code for `Organization` entity, request/response DTOs, repository, service, controller, and duplicate-slug conflict exception.
- Updated global exception handler to return HTTP 409 for duplicate organization slugs.
- Organization CRUD remains pending local compile and Postman verification.
- No claim of tenant isolation yet; users, memberships, project-organization association, authentication, and authorization remain future work.


### 2026-10-27 — Organization CRUD verified
- User confirmed organization CRUD tests all work.
- Marked organization CRUD as verified.
- Proceeded to user and organization-membership domain model.

### 2026-10-27 — User and membership model code provided
- Added implementation instructions for `AppUser`, `OrganizationRole`, `OrganizationMembership`, and their repositories.
- This step is pending local compilation and application-startup verification.
- No registration/login endpoints or authorization behavior are claimed complete.


### 2026-10-27 — User and membership model verified
- User confirmed the app starts successfully after adding `AppUser`, `OrganizationRole`, `OrganizationMembership`, and their repositories.
- Marked the model/startup verification complete.

### 2026-10-27 — Project-organization association code provided
- Provided updated `Project`, `ProjectRequest`, `ProjectResponse`, `ProjectRepository`, and `ProjectService` implementations.
- Added `OrganizationProjectController` for listing projects by organization.
- Implementation is pending user's local compile and Postman verification. Existing projects can have a null organization temporarily; new project requests require an existing organization ID.

## 14. Current State Override — 2026-10-28

This section supersedes any older status statements above where they conflict with the current verified project progress.

### Confirmed implementation and API testing
- User confirmed organization CRUD and validation/error handling work.
- User confirmed JWT registration and login work in Postman.
- User confirmed membership API behavior and resolved a DELETE authorization issue.
- User confirmed project/task tenant-access implementation works through their ongoing API verification. Project access policy: OWNER/ADMIN/MEMBER may view projects; OWNER/ADMIN may create, update, or delete projects. Task operations are allowed for organization members; assignees must belong to the same organization; project/task moves across organizations are rejected.

### Automated unit tests — confirmed by user on 2026-10-28
JUnit 5 and Mockito test suites have been created and run successfully in Eclipse:
- `OrganizationAccessServiceTest`: 5 tests passed.
- `ProjectServiceTest`: 7 tests passed.
- `TaskServiceTest`: 8 tests passed.
- Total: 20 tests passed, 0 failures reported by the user.

Coverage includes organization membership and role checks, accessible organization IDs, project update/delete authorization, denying project access without membership, preventing project moves across organizations, task creation and update, assignee membership validation, task deletion, empty search results when no organizations are accessible, and missing-resource handling.

These are service unit tests with mocked dependencies. They do not replace integration tests against a running Spring Boot application/PostgreSQL or HTTP-level end-to-end tests. Mockito's shared setup stubs were marked lenient where necessary to avoid unnecessary-stubbing errors; this does not suppress assertions or exceptions from the tested service logic.

### Recommended next steps
1. Add integration tests for authentication, organization membership, project/task tenant isolation, and HTTP status/error responses.
2. Review test coverage for positive and negative cases, including project creation, task filtering with non-empty organization IDs, and cross-tenant requests.
3. Add CI to run `mvn test` on every push/pull request.
4. Continue production readiness: externalize secrets, Docker Compose, logging, migrations, deployment instructions, and README/resume summary.

### Workflow reminder
Do not mark any additional milestone complete until the user confirms the relevant build/tests or API checks have passed. Never include real passwords, JWTs, or secrets in this file.

## 15. Change Log — 2026-10-28
- Added `OrganizationAccessServiceTest`: 5/5 tests passed, confirmed by user.
- Added `ProjectServiceTest`: 7/7 tests passed, confirmed by user.
- Added `TaskServiceTest`: 8/8 tests passed, confirmed by user after resolving Mockito setup and repository verification issues.
- Automated unit-test total now stands at 20 passing tests across the three suites.

## 16. Current State Override — 2026-10-28 (Integration Tests Verified)

### Project/task security integration tests — confirmed by user
- User ran `ProjectTaskSecurityIntegrationTest` in Eclipse and confirmed all 17 tests passed successfully.
- The suite uses Spring Boot integration testing with MockMvc and an H2 test database under the `test` profile.
- Coverage includes unauthenticated requests returning HTTP 401, invalid JWT rejection, project/task list tenant isolation, denial of access to another organization's individual project/task, member restrictions on project write operations, authorized project/task creation, cross-organization task creation/moves, invalid assignee rejection, and DTO validation errors.
- The invalid-assignee test was corrected to expect HTTP 400 (`Bad Request`) because the service raises `IllegalArgumentException` for an assignee outside the task's organization and the global exception handler maps it to 400. The request is rejected as intended.
- Confirmed automated test totals: 20 service unit tests + 17 integration tests = 37 passing tests, with no failures reported by the user.
- These integration tests run against H2 in PostgreSQL compatibility mode; they are not a substitute for a final smoke test against the configured PostgreSQL database or a deployed environment.

### Recommended next step
- Add CI to run `mvn test` automatically, then continue API documentation and frontend integration or production-readiness work. Confirm each new step with the user before marking it complete.

## 17. Change Log — 2026-10-28
- Added 10 additional project/task security integration tests to the existing integration suite, bringing it to 17 tests.
- User confirmed all 17 integration tests pass successfully after correcting the invalid-assignee expectation from HTTP 403 to HTTP 400.
- Total confirmed automated tests: 37 passed (20 unit tests and 17 integration tests).
