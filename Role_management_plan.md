# Role Management Feature Plan (Backend + Frontend)

**Repo**: `Learning/` monorepo
- **Frontend**: `grace-manage-rwc` (Angular 18 standalone)
- **Backend**: `grace-manage-service` (Spring Boot 4, clean architecture)

**Date**: 2026-01-16
**Status**: Planning

---

## 0) Context / Current State

### Frontend (current)
- Role Management UI is implemented (`grace-manage-rwc/src/app/role-management/*`).
- `RoleService` (`grace-manage-rwc/src/app/role.service.ts`) is **mocked**:
  - Stores roles in **localStorage**.
  - Simulates latency via `delay(300)`.
  - Provides CRUD-like methods (create/update/delete/getById/getRolesOnce/getRoles stream).
- Permissions are currently hardcoded in FE and (in docs) described as **18 permissions**, but `RoleService.getAvailablePermissions()` currently includes **extra stock permissions** (`view_stocks`, `update_stocks`, `delete_stocks`).

### Backend (current)
- Backend currently has **User** management only.
- Authentication appears to be JWT-based already (see `JwtTokenProvider`, `JwtAuthenticationFilter`, `AuthController`).
- Spring Security:
  - `/api/v1/auth/**` permitAll
  - `/api/v1/users/**` requires role ADMIN or USER
  - everything else authenticated
- Existing DB table `T_USER` has a `role` string column with a check constraint: `role IN ('admin','user','viewer')` (Flyway V3).

---

## 1) Requirements (from request)

- Implement Role Management **Backend + Frontend**.
- A **Role** can have many **Permissions**.
- There are **18 permissions** (as per FE role-management docs).
- Backend develop REST APIs: GET list, GET detail, POST, PATCH, DELETE, etc.
- Frontend: adapt current local storage mocked logic to real REST API calls.
- Create a Flyway script to add an **ADMIN role** by default (typo in request says AMIN).
- **ALL Role Management APIs require ADMIN** to access.

---

## 2) Proposed Data Model (Backend)

### 2.1 Permission model
Use a **fixed set** of permissions (18) enforced by code and optionally stored in DB.

**Source of truth recommendation**:
- Backend defines an enum `PermissionCode` (18 values).
- Backend exposes `GET /api/v1/permissions` returning the list for UI.
- DB stores assigned permissions per role.

### 2.2 Role model
A role has:
- `id` (Long)
- `name` (string, unique, case-insensitive recommended)
- `description` (string)
- `permissions` (set of permission codes)
- `createdAt`, `updatedAt`

### 2.3 Database schema (SQL Server)
Recommended tables:
- `T_ROLE`
  - `id BIGINT IDENTITY PRIMARY KEY`
  - `name VARCHAR(100) NOT NULL UNIQUE`
  - `description VARCHAR(500) NULL`
  - `created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()`
  - `updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()`
- `T_ROLE_PERMISSION`
  - `role_id BIGINT NOT NULL` (FK -> `T_ROLE.id`)
  - `permission_code VARCHAR(100) NOT NULL`
  - `PRIMARY KEY (role_id, permission_code)`

Add indexes:
- `idx_role_name` on `T_ROLE(name)`
- `idx_role_permission_code` on `T_ROLE_PERMISSION(permission_code)`

**Note:** keep `T_USER.role` string as-is for now (minimal disruption). Long-term we can migrate users to reference `T_ROLE`.

---

## 3) Permissions (18) — canonical list

Per `grace-manage-rwc/src/app/role-management/docs/ROLE_MANAGEMENT.md`:

1. `view_employees`
2. `create_employee`
3. `edit_employee`
4. `delete_employee`
5. `view_roles`
6. `create_role`
7. `edit_role`
8. `delete_role`
9. `view_statistics`
10. `view_check_in_out`
11. `manage_check_in_out`
12. `manage_inventory`
13. `view_notifications`
14. `manage_notifications`
15. `export_data`
16. `import_data`
17. `system_settings`
18. `user_management`

**Action item**: FE currently returns extra permissions in `RoleService.getAvailablePermissions()`; update it to match these 18 once using backend.

---

## 4) Backend Implementation Plan (Clean Architecture)

Follow the `User` feature structure described in `CLAUDE.md`.

### 4.1 Domain Layer (`domain/`)
Create domain entities + repository interfaces + use cases.

**Entities**
- `Role` (domain entity)
  - fields: `id`, `name`, `description`, `permissions(Set<String>)`, `createdAt`, `updatedAt`

**Repository interface**
- `RoleRepository`
  - `List<Role> findAll()`
  - `Optional<Role> findById(Long id)`
  - `Optional<Role> findByName(String name)`
  - `Role save(Role role)`
  - `void deleteById(Long id)`
  - `boolean existsById(Long id)`

**Use cases**
- `ListRolesUseCase`
- `GetRoleUseCase`
- `CreateRoleUseCase`
- `UpdateRoleUseCase` (PATCH semantics)
- `DeleteRoleUseCase`

**Domain-level validation rules**
- `name` required, 3–50 chars (match FE constraints) and unique.
- `description` required, 10–500 chars (match FE constraints).
- `permissions` required, min 1.
- `permissions` must be from the canonical set of 18.

### 4.2 Application Layer (`application/`)

**DTOs**
- `RoleDTO` (internal)
- `CreateRoleDTO` / `UpdateRoleDTO` (optional)

**Mapper**
- `RoleMapper` (MapStruct): domain ↔ DTO

**Service**
- `RoleApplicationService`
  - orchestrates use cases
  - provides transactional boundaries

**Exceptions**
- Reuse existing `ValidationException`, `ResourceNotFoundException`.

### 4.3 Infrastructure Layer (`infrastructure/`)

**JPA entities**
- `RoleEntity` mapped to `T_ROLE`
- `RolePermissionEntity` (or `@ElementCollection`) mapped to `T_ROLE_PERMISSION`

**Repository implementation**
- `RoleRepositoryImpl` implements domain `RoleRepository`

**Spring Data JPA repository**
- `RoleJpaRepository extends JpaRepository<RoleEntity, Long>`

**Conversion methods**
- `RoleEntity.fromDomain(Role)` and `RoleEntity.toDomain()` (like `UserEntity` pattern)

### 4.4 Presentation Layer (`presentation/`)

**Requests**
- `CreateRoleRequest`
  - `name`, `description`, `permissions` with Jakarta validation
- `UpdateRoleRequest`
  - optional `name`, `description`, `permissions`

**Responses**
- `RoleResponse` with FE-friendly fields
  - Consider timestamps: FE currently uses `Date`; backend can return ISO string or epoch.
  - Existing FE `ApiResponse<T>` expects `data` and `message`.

**Controller**
- `RoleController` under `/api/v1/roles`

### 4.5 Endpoint Design (Backend)

All endpoints return `ApiResponse<T>`.

**Role CRUD**
- `GET /api/v1/roles`
  - returns list
- `GET /api/v1/roles/{id}`
  - returns single role
- `POST /api/v1/roles`
  - creates role
- `PATCH /api/v1/roles/{id}`
  - partial update (name/description/permissions)
- `DELETE /api/v1/roles/{id}`
  - delete by id

**Permissions list for UI**
- `GET /api/v1/permissions`
  - returns list of 18 permission codes (and optionally labels)

**Optional enhancements** (nice-to-have)
- `GET /api/v1/roles?search=...` (server-side filtering)
- `GET /api/v1/roles/name/{name}`

### 4.6 Security: ADMIN-only access

Requirement: **ALL Role Management APIs need ADMIN role**.

Implementation options:
1) **Route matcher** in `SecurityConfig`:
   - `.requestMatchers("/api/v1/roles/**", "/api/v1/permissions/**").hasRole("ADMIN")`
2) **Method-level** security:
   - `@PreAuthorize("hasRole('ADMIN')")` on controller methods

Recommendation: do BOTH for defense-in-depth, but route matcher is sufficient if consistent.

Important note about current roles:
- Backend currently uses `T_USER.role` values like `admin` and converts them to Spring authorities: `ROLE_ADMIN`.
- So to satisfy ADMIN-only, the calling user must have `role = 'admin'`.

---

## 5) Flyway Migration Plan

### 5.1 Create schema for roles & permissions
Add a migration (next version after V4):
- `V5__Create_Role_And_Permission_Tables.sql`
  - Create `T_ROLE`
  - Create `T_ROLE_PERMISSION`
  - Add necessary indexes + FK

### 5.2 Seed default ADMIN role
Add a migration:
- `V6__dml_seed_admin_role.sql`
  - Insert `ADMIN` role record in `T_ROLE`
  - Insert the 18 permission rows for that role in `T_ROLE_PERMISSION`

**Idempotency recommendation** (important in Flyway):
- Use SQL that checks existence before insert.
- Example approach:
  - `IF NOT EXISTS (SELECT 1 FROM T_ROLE WHERE name = 'ADMIN') INSERT ...`

### 5.3 Align with existing admin user
You already have `V4__dml_add_admin_account.sql` inserting a user with role `admin`.
- Keep it.
- Ensure ADMIN role name convention is consistent:
  - User uses `admin` (to satisfy security)
  - Role table record can be `ADMIN` or `Admin` (but pick one). Recommendation: store `ADMIN`.

---

## 6) Frontend Implementation Plan (Angular 18)

### 6.1 Target behavior
Keep the current UI/UX:
- Table listing, loading spinners
- Create/edit modal, validation
- Delete confirm
- Permission checkboxes

But replace localStorage mock with backend calls.

### 6.2 API integration pattern
Frontend already has:
- `AuthService` storing tokens in localStorage.
- `AuthInterceptor` exists (`src/app/interceptors/auth.interceptor.ts`) so role management calls should automatically carry `Authorization: Bearer <accessToken>`.

Action items:
- Ensure `RoleService` uses `HttpClient`.
- Ensure base URL uses `environment.apiUrl`.
- Use `ApiResponse<T>` interface from `src/app/shared/models/api-response.model.ts`.

### 6.3 Refactor `RoleService` (`grace-manage-rwc/src/app/role.service.ts`)

**New service contract** (recommended)
- `getRolesOnce(): Observable<Role[]>`
  - call `GET /api/v1/roles`
- `getRoleById(id): Observable<Role | null>`
  - call `GET /api/v1/roles/{id}`
- `createRole(payload): Observable<RoleResponse>`
  - call `POST /api/v1/roles`
- `updateRole(id, payload): Observable<RoleResponse>`
  - call `PATCH /api/v1/roles/{id}`
- `deleteRole(id): Observable<RoleResponse>`
  - call `DELETE /api/v1/roles/{id}`
- `getAvailablePermissions(): Observable<string[]>`
  - call `GET /api/v1/permissions`

**BehaviorSubject strategy**
Currently `RoleService` maintains an internal `rolesSubject`.
Options:
- A) Keep it: after any mutation, refresh list from server and emit.
- B) Simplify: remove the subject and let components fetch on demand.

Recommendation: keep it for minimal UI changes.

### 6.4 Update `RoleManagementComponent`
It already calls:
- `getRolesOnce()`
- `getAvailablePermissions()`
- `createRole() / updateRole() / deleteRole()`

So the UI should need minimal changes if service signatures are preserved.

### 6.5 Fix permission list mismatch
FE docs say 18, FE code currently returns 21.
- After switching to backend `/permissions`, FE will naturally render correct 18.
- Also update any docs/tests expecting the old list.

### 6.6 Update other feature dependencies
`EmployeeDetailComponent` loads roles to map roleId→role name.
- Decide what `Employee.role` is in FE:
  - If employees store roleId, continue supporting `Role[]` list.
  - If employees store role string, update mapping.

Action: audit employee model & storage to ensure role linkage is consistent.

---

## 7) API Contract (Suggested)

### 7.1 `RoleResponse` (backend)
Return a structure compatible with FE expectations:

```json
{
  "status": "success",
  "message": "Operation successful",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Full system access",
    "permissions": ["view_employees", "create_employee"],
    "createdAt": "2026-01-16T00:00:00Z",
    "updatedAt": "2026-01-16T00:00:00Z"
  }
}
```

### 7.2 `GET /api/v1/permissions`
```json
{
  "status": "success",
  "message": "Operation successful",
  "data": ["view_employees", "create_employee", "..."]
}
```

### 7.3 Error responses
Use existing `GlobalExceptionHandler` pattern:
- `status = "error"`
- meaningful `message`
- validation error details if already supported

---

## 8) Testing Strategy

### Backend
- Unit tests for use cases:
  - validate permissions are restricted to the 18
  - enforce name uniqueness
  - update patch behavior
- Controller tests:
  - unauthorized (no token) → 401
  - non-admin token → 403
  - admin token → success

### Frontend
- Update `role.service.spec.ts`:
  - switch tests to `HttpClientTestingModule`
  - mock API responses
  - ensure methods map `ApiResponse<T>.data` correctly
- Small smoke test:
  - load roles list
  - create/edit/delete flows

---

## 9) Rollout / Migration Notes

- This plan introduces role tables but does not refactor existing user `role` string.
- Security for role APIs depends on user’s `role` claim being `admin` → `ROLE_ADMIN`.
- If later you want users assigned to dynamic roles:
  - add `role_id` to `T_USER`
  - migrate existing values
  - compute authorities from role permissions

---

## 10) Checklist (Implementation Order)

### Backend
- [x] Add Flyway `V5__Create_Role_And_Permission_Tables.sql`
- [x] Add Flyway `V6__dml_seed_admin_role.sql` (ADMIN role + 18 permissions)
- [x] Add domain: `Role` + `PermissionCode` + `RoleRepository` + use cases
- [x] Add application: `RoleDTO`, mapper, application service
- [x] Add infra: JPA entities + repository impl
- [x] Add presentation: controller + request/response DTOs
- [x] Secure endpoints (ADMIN-only)
- [ ] Add tests

### Frontend
- [ ] Refactor `RoleService` from localStorage to HttpClient REST calls
- [ ] Ensure permission list shows exactly 18 (from `GET /permissions`)
- [ ] Keep existing UI behavior in `RoleManagementComponent`
- [ ] Update unit tests

---

## 11) Open Decisions (small, but important)

1) **Role name canonical form**: store/display `ADMIN` vs `Admin`.
   - Recommendation: store uppercase in DB but display with title case in UI if desired.

2) **Deletion rules**:
   - Recommended guardrails:
     - Disallow deleting `ADMIN`
     - Disallow deleting roles that are assigned to any user (if/when user-role linking exists)

3) **Timestamps type**:
   - Decide ISO strings vs epoch millis.
   - Recommendation: ISO strings from backend; FE converts to `Date`.

