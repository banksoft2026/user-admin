# user-admin — Prompt History & Development Log

This service handles authentication, user management, role-based access control (RBAC), permissions, and approval workflows for the BankSoft CBS platform.

- **Port:** 8084
- **Base URL:** `http://localhost:8084`
- **Database:** PostgreSQL
- **Framework:** Spring Boot 3 / Java 21
- **Security:** JWT (stateless), BCrypt password hashing

---

## Prompt 1 — Initial Service Build

**Prompt:**
> "Build the user-admin microservice with JWT authentication, RBAC, and approval workflow APIs."

### Steps Taken
1. Created Spring Boot project with dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL, Lombok, JJWT, Validation, Actuator, SpringDoc OpenAPI
2. Defined domain entities:
   - `User` — system users with username, password (BCrypt), email, roles
   - `Role` — named roles (ADMIN, CHECKER, MAKER, TELLER, etc.)
   - `Permission` — granular screen/action permissions
   - `RolePermission` — many-to-many role ↔ permission mapping
   - `ApprovalRequest` — pending dual-control approval records
   - `AuditLog` — immutable audit trail of all user actions
3. Implemented JWT:
   - `JwtUtil` — token generation, validation, claims extraction
   - `JwtAuthenticationFilter` — `OncePerRequestFilter` extracting Bearer token and setting `SecurityContext`
4. Configured `SecurityConfig`:
   - CSRF disabled (stateless API)
   - CORS enabled (`allowedOriginPatterns("*")`)
   - Session management: `STATELESS`
   - Permit: `/v1/auth/login`, `/v1/auth/refresh`, actuator health/info, Swagger
   - All other requests: authenticated
5. Created all DTOs, repositories, services, controllers
6. Configured `application.yml` with datasource, JWT secret, token expiry, server port 8084

### Key API Endpoints
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/auth/login` | Public | Authenticate, returns JWT |
| POST | `/v1/auth/refresh` | Public | Refresh access token |
| GET | `/v1/users` | Authenticated | List users (paginated) |
| POST | `/v1/users` | ADMIN | Create user |
| GET | `/v1/users/{id}` | Authenticated | Get user detail |
| PATCH | `/v1/users/{id}/status` | ADMIN | Activate/deactivate user |
| GET | `/v1/roles` | Authenticated | List roles |
| POST | `/v1/roles` | ADMIN | Create role |
| GET | `/v1/permissions/screens` | Authenticated | List screen permissions |
| POST | `/v1/permissions/screens` | ADMIN | Create screen permission |
| GET | `/v1/approvals/pending` | Authenticated | List pending approvals |
| GET | `/v1/approvals/pending-count` | Authenticated | Count pending approvals |
| POST | `/v1/approvals/{id}/approve` | CHECKER/ADMIN | Approve a request |
| POST | `/v1/approvals/{id}/reject` | CHECKER/ADMIN | Reject a request |

---

## Prompt 2 — README Documentation

**Prompt:**
> "Prep readme.md files in git for user-admin and bank ops in respective repos."

### Steps Taken
1. Created comprehensive `README.md` covering:
   - Service overview and JWT auth flow
   - Default admin credentials
   - Prerequisites and setup instructions
   - All API endpoints with auth requirements
   - JWT token structure and claims
2. Committed and pushed to GitHub (`banksoft2026/user-admin`)

---

## Prompt 3 — Fix 403 Errors From Frontend

**Prompt:**
> "Errors on UI — port 8084 returning 403 on all requests from authenticated pages."

### Root Cause
Spring Security in `STATELESS` session mode returns **HTTP 403** by default for both:
1. **Unauthenticated requests** (no JWT token / expired token) — should return **401**
2. **Authorised but insufficient role** — should return **403**

With no `authenticationEntryPoint` configured, both cases returned 403, preventing the frontend from distinguishing between "not logged in" and "access denied". The axios interceptor in the frontend was listening for `401` to trigger logout/redirect — which never fired.

### Steps Taken

#### `SecurityConfig.java`
Added `exceptionHandling` configuration to `SecurityFilterChain`:

```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((request, response, authException) -> {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"status\":401,\"message\":\"Unauthorised — please log in\"}"
        );
    })
    .accessDeniedHandler((request, response, accessDeniedException) -> {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);      // 403
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"status\":403,\"message\":\"Access denied — insufficient permissions\"}"
        );
    })
)
```

This ensures:
- Expired/missing JWT → **401 Unauthorized**
- Valid JWT but wrong role → **403 Forbidden**

#### Frontend `api.ts` (banking-ops-ui)
The 401 interceptor was updated to also clear `auth-storage` (Zustand persist key):
```js
if (error.response?.status === 401) {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('auth-storage');  // Clear Zustand persist so RouteGuard redirects
  window.location.href = '/login';
}
```

### Files Modified
- `src/main/java/com/banksoft/useradmin/config/SecurityConfig.java`

### Issues Resolved
| Issue | Root Cause | Fix |
|-------|-----------|-----|
| All requests returning 403 regardless of auth state | No `authenticationEntryPoint` — Spring Security defaulted all rejections to 403 | Added explicit entry point returning 401 for missing/invalid JWT |
| Frontend not redirecting to login on token expiry | Axios interceptor only caught 401, but service was returning 403 | Fixed service to return correct 401 for unauthenticated requests |
| `isAuthenticated` staying `true` after token expiry | Zustand `auth-storage` persist key not cleared on logout | Added `localStorage.removeItem('auth-storage')` in 401 interceptor |

---

## Prompt 4 — Service Restart

**Prompt:**
> "Restart all services after CORS fix."

### Steps Taken
1. Existing process on port 8084 killed
2. `git pull` — already up to date
3. Started with Maven + Java 21 — **success**
4. Service confirmed UP: `GET /actuator/health` → HTTP 200

### Issues Resolved
| Issue | Fix |
|-------|-----|
| No `mvnw` file | Used full path to Maven |
| Java 25 incompatible with Spring compiler plugin | Used Java 21 (`jdk-21.0.6+7`) |

---

## Security Architecture

### JWT Flow
```
Client → POST /v1/auth/login { username, password }
       ← 200 { accessToken, refreshToken, userId, username, roles, ... }

Client → GET /v1/users (Authorization: Bearer <accessToken>)
       JwtAuthenticationFilter extracts token
       JwtUtil validates + parses claims
       SecurityContext populated with userId + ROLE_XXX authorities
       ← 200 { data: [...] }

Client → POST /v1/auth/refresh { refreshToken }
       ← 200 { accessToken, refreshToken }
```

### Token Claims
| Claim | Description |
|-------|-------------|
| `sub` | userId (UUID) |
| `roles` | List of role names |
| `iat` | Issued at (epoch seconds) |
| `exp` | Expiry (epoch seconds) |

### Default Roles
| Role | Description |
|------|-------------|
| ADMIN | Full system access |
| MAKER | Can create/submit transactions and records |
| CHECKER | Can approve/reject pending requests |
| TELLER | Branch transaction operations |
| BRANCH_MANAGER | Branch-level management |
| SENIOR_MANAGER | Board-level approvals |
| AUDITOR | Read-only audit access |
| VIEWER | Read-only access |
