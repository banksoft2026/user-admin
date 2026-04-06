# user-admin

User administration microservice for the CBS (Core Banking System) platform. Handles authentication, user lifecycle, role-based access control, a 3-layer permission model, maker-checker workflow, session management, and an immutable audit log.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| Build | Maven 3.9.6 |
| Database | PostgreSQL 18 |
| Migrations | Flyway 10.10.0 |
| Security | Spring Security 6 + JWT (jjwt 0.12.5 / HS512) |
| API Docs | SpringDoc OpenAPI 2.4.0 (Swagger UI) |

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 18 running locally on port `5432`

### 1. Create the database

```sql
CREATE DATABASE user_admin_db;
```

### 2. Configure credentials

Edit `src/main/resources/application.yml` if your PostgreSQL username/password differ from the defaults:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_admin_db
    username: postgres
    password: banksoft@2026
```

### 3. Run the service

```bash
mvn spring-boot:run
```

The service starts on **port 8084**. Flyway applies all 10 migrations automatically on startup.

### 4. Default admin credentials

A default admin user is seeded by `DataInitializer` on first boot:

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `Admin@2026` |
| Role | `ADMIN` |

---

## API Reference

Interactive docs available at: `http://localhost:8084/swagger-ui/index.html`

### Authentication — `/v1/auth`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/v1/auth/login` | Authenticate and receive access + refresh tokens |
| `POST` | `/v1/auth/refresh` | Exchange refresh token for new access token |
| `POST` | `/v1/auth/logout` | Invalidate current session |
| `POST` | `/v1/auth/change-password` | Change authenticated user's password |

**Login request:**
```json
POST /v1/auth/login
{
  "username": "admin",
  "password": "Admin@2026"
}
```

**Login response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": "...",
    "username": "admin",
    "roles": ["ADMIN"]
  }
}
```

> All subsequent requests must include `Authorization: Bearer <accessToken>`.

---

### Users — `/v1/users`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/v1/users` | List all users (paginated) |
| `POST` | `/v1/users` | Create new user |
| `GET` | `/v1/users/{id}` | Get user by ID |
| `PUT` | `/v1/users/{id}` | Update user details |
| `POST` | `/v1/users/{id}/lock` | Lock user account |
| `POST` | `/v1/users/{id}/unlock` | Unlock user account |
| `POST` | `/v1/users/{id}/suspend` | Suspend user |
| `POST` | `/v1/users/{id}/activate` | Activate user |
| `POST` | `/v1/users/{id}/reset-password` | Admin password reset |

---

### Roles — `/v1/roles`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/v1/roles` | List all active roles |
| `POST` | `/v1/roles` | Create custom role |
| `GET` | `/v1/roles/{id}` | Get role by ID |
| `PUT` | `/v1/roles/{id}` | Update role (non-system roles only) |
| `DELETE` | `/v1/roles/{id}` | Delete role (non-system roles only) |
| `GET` | `/v1/roles/{id}/users` | List users assigned to role |
| `POST` | `/v1/roles/assign` | Assign role to user |
| `DELETE` | `/v1/roles/revoke` | Revoke role from user |

---

### Permissions — `/v1/permissions`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/v1/permissions/modules` | List all modules and screens |
| `GET` | `/v1/permissions/screens` | List all screens |
| `GET` | `/v1/permissions/screen-perms` | Get screen permissions for a role |
| `PUT` | `/v1/permissions/screen-perms` | Update screen permission for a role |
| `GET` | `/v1/permissions/field-perms` | Get field permissions for a role/screen |
| `PUT` | `/v1/permissions/field-perms` | Update field permission |
| `GET` | `/v1/permissions/user/{id}` | Resolve effective permissions for a user |

---

### Maker-Checker — `/v1/maker-checker`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/v1/maker-checker/submit` | Submit action for approval |
| `GET` | `/v1/maker-checker/pending` | List pending queue entries |
| `POST` | `/v1/maker-checker/{id}/approve` | Approve entry (checker only, no self-approve) |
| `POST` | `/v1/maker-checker/{id}/reject` | Reject entry with reason |
| `POST` | `/v1/maker-checker/{id}/recall` | Recall a pending submission |

---

### Audit Log — `/v1/audit-log`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/v1/audit-log` | Query audit log (paginated, immutable) |

---

## Database Schema

Migrations live in `src/main/resources/db/migration/`:

| Migration | Description |
|---|---|
| `V1__users.sql` | `users` table with status CHECK constraint |
| `V2__roles.sql` | `roles` table + seeds 4 system roles |
| `V3__user_roles.sql` | `user_roles` junction table |
| `V4__modules_screens.sql` | `modules` + `screens` tables, seeds 9 modules / 24 screens |
| `V5__screen_fields.sql` | `screen_fields` for field-level permission registration |
| `V6__role_screen_perms.sql` | `role_screen_perms` + default grants per system role |
| `V7__field_permissions.sql` | `field_permissions` (VISIBLE/MASKED/HIDDEN, EDITABLE/READ_ONLY) |
| `V8__maker_checker_queue.sql` | `maker_checker_queue` with no-self-approve DB constraint |
| `V9__sessions_attempts.sql` | `user_sessions` + `auth_attempts` for lockout tracking |
| `V10__audit_log.sql` | `audit_log` with indexed columns |

---

## Security Model

### JWT Tokens

| Token | Algorithm | Expiry |
|---|---|---|
| Access token | HS512 | 15 minutes |
| Refresh token | HS512 | 8 hours |

Refresh tokens are stored as SHA-256 hashes in `user_sessions` — raw tokens are never persisted.

### Account Lockout

After **5 consecutive failed logins**, an account is locked for **30 minutes** automatically. Admins can unlock manually via `POST /v1/users/{id}/unlock`.

### System Roles

Four roles are seeded at migration time and cannot be modified or deleted:

| Role | Level | Description |
|---|---|---|
| `ADMIN` | 1 | Full system access including user management |
| `CHECKER` | 2 | Approve/reject maker-checker workflows |
| `MAKER` | 3 | Create and submit actions for approval |
| `VIEWER` | 4 | Read-only access, sensitive fields masked |

### Permission Resolution

Permissions are resolved at three layers with **most-permissive wins** logic:

1. **Screen level** — Can the user access this screen at all?
2. **Action level** — `FULL` / `MAKER` / `READ` / `NONE`
3. **Field level** — Per-field `VISIBLE` / `MASKED` / `HIDDEN` + `EDITABLE` / `MAKER_ONLY` / `READ_ONLY`

A user with multiple roles inherits the union of their permissions across all assigned roles.

---

## Project Structure

```
src/main/java/com/banksoft/useradmin/
├── auth/               # Login, refresh, logout, change-password
├── user/               # User CRUD and lifecycle management
├── role/               # Role management and user-role assignments
├── permission/         # Module/screen/field permission resolution
├── makerchecker/       # Maker-checker workflow queue
├── audit/              # Audit log querying
├── common/             # ApiResponse, CbsException, JwtUtil, GlobalExceptionHandler
├── config/             # SecurityConfig, JwtAuthenticationFilter, JdbcConfig
└── init/               # DataInitializer (seeds admin user on first boot)
```

---

## Health Check

```
GET http://localhost:8084/actuator/health
```
