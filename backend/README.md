# Royal Pearl Hyderabad — Hotel Management REST API

Production-ready Spring Boot 3.3 / Java 21 backend for the Royal Pearl Hyderabad luxury hotel at Road No. 12, Banjara Hills, Hyderabad, Telangana.

---

## Table of Contents

1. [Quick Start (Docker)](#1-quick-start-docker)
2. [Run Locally Without Docker](#2-run-locally-without-docker)
3. [Run Tests](#3-run-tests)
4. [Environment Variables](#4-environment-variables)
5. [Database Migrations](#5-database-migrations)
6. [Project Structure](#6-project-structure)
7. [Architecture Decisions](#7-architecture-decisions)
8. [API Endpoint Reference](#8-api-endpoint-reference)
9. [Sample curl Requests](#9-sample-curl-requests)
10. [Security Model](#10-security-model)
11. [Acceptance Criteria Checklist](#11-acceptance-criteria-checklist)

---

## 1. Quick Start (Docker)

**Prerequisites:** Docker Desktop ≥ 24, Docker Compose v2.

```bash
# 1. Clone / enter the backend directory
cd backend

# 2. Copy and populate the env file
cp .env.example .env
# Edit .env: set DB_PASSWORD and JWT_SECRET at minimum

# 3. Build and start (first run takes ~3 min to download images + build)
docker compose up --build -d

# 4. Tail logs
docker compose logs -f api

# 5. Verify
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# 6. Open Swagger UI
# http://localhost:8080/swagger-ui.html
```

Flyway migrations (`V1__init.sql` schema + `V2__seed.sql` seed data) run automatically on startup.

To stop and remove all containers and volumes:
```bash
docker compose down -v
```

---

## 2. Run Locally Without Docker

**Prerequisites:** Java 21, Maven 3.9+, PostgreSQL 14+.

```bash
# 1. Create the database
psql -U postgres -c "CREATE DATABASE royalpearl;"
psql -U postgres -c "CREATE USER royalpearl WITH PASSWORD 'royalpearl';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE royalpearl TO royalpearl;"

# 2. Export required environment variables (PowerShell)
$env:DB_URL       = "jdbc:postgresql://localhost:5432/royalpearl"
$env:DB_USERNAME  = "royalpearl"
$env:DB_PASSWORD  = "royalpearl"
$env:JWT_SECRET   = "my-local-dev-secret-at-least-32-chars-long!"
$env:ADMIN_ALLOWLIST = "bashamahemood579@gmail.com"

# 3. Build (skipping tests for speed)
mvn package -DskipTests

# 4. Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the fat JAR directly
java -jar target/hotel-1.0.0.jar --spring.profiles.active=dev
```

The API starts on port **8080** by default.

---

## 3. Run Tests

Tests use **Testcontainers** — Docker must be running.

```bash
# Run all tests (spins up a PostgreSQL container automatically)
mvn test

# Run a specific test class
mvn test -Dtest=BookingIntegrationTest

# Run only unit tests (no container needed)
mvn test -Dtest=ReferenceGeneratorTest

# Run with verbose output
mvn test -Dsurefire.useFile=false
```

Test suite covers:
| Test Class | What it verifies |
|---|---|
| `AuthFlowIntegrationTest` | Register, login, token refresh rotation, duplicate email 409, logout |
| `BookingIntegrationTest` | RP reference format/uniqueness/DB persistence, paymentStatus always pending, 422 validation, lookup |
| `OrderIntegrationTest` | RPO- reference, paymentStatus protection, empty-cart 422, lookup |
| `AdminAccessControlTest` | 403 for non-admin on every `/admin/**`, admin mark-paid, exact ref search, user sees own records only |
| `AdminPaginationTest` | Spring Page structure, page/size params respected |
| `ReferenceGeneratorTest` | Pattern, 7-digit range, ≥99% uniqueness over 1000 samples |

---

## 4. Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_URL` | ✓ | — | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/royalpearl` |
| `DB_USERNAME` | ✓ | — | PostgreSQL username |
| `DB_PASSWORD` | ✓ | — | PostgreSQL password |
| `DB_NAME` | docker only | `royalpearl` | Database name (used by Compose to create the PG container) |
| `DB_PORT` | docker only | `5432` | Host port mapped to PostgreSQL |
| `JWT_SECRET` | ✓ | — | HS256 signing key, **minimum 32 characters** |
| `JWT_ACCESS_TTL` | | `900` | Access token TTL in seconds (default 15 min) |
| `JWT_REFRESH_TTL` | | `604800` | Refresh token TTL in seconds (default 7 days) |
| `CORS_ALLOWED_ORIGINS` | | `http://localhost:5173` | Comma-separated frontend origins |
| `ADMIN_ALLOWLIST` | | `bashamahemood579@gmail.com` | Comma-separated emails that may claim admin via `/me/claim-admin` |
| `FRONTEND_URL` | | `http://localhost:5173` | Base URL for OAuth2 redirect |
| `GOOGLE_CLIENT_ID` | | `disabled` | Google OAuth2 client ID (set to enable) |
| `GOOGLE_CLIENT_SECRET` | | `disabled` | Google OAuth2 client secret |
| `SPRING_PROFILES_ACTIVE` | | `dev` | Active profile: `dev` or `prod` |
| `API_PORT` | docker only | `8080` | Host port mapped to the API |

Generate a strong JWT secret:
```bash
openssl rand -base64 64
```

---

## 5. Database Migrations

Flyway manages all schema changes under `src/main/resources/db/migration/`.

| File | Contents |
|---|---|
| `V1__init.sql` | Full schema: pgcrypto extension, `app_role` enum, `set_updated_at()` trigger, all 8 tables with constraints and indexes |
| `V2__seed.sql` | 4 room types + 54 menu items across 8 categories |

**Flyway runs automatically** on application startup. To run migrations manually:
```bash
mvn flyway:migrate -Dflyway.url=$DB_URL \
                   -Dflyway.user=$DB_USERNAME \
                   -Dflyway.password=$DB_PASSWORD
```

To inspect migration state:
```bash
mvn flyway:info ...
```

Adding new migrations: create `V3__description.sql` in the same directory and restart the app.

---

## 6. Project Structure

```
backend/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── src/
    ├── main/
    │   ├── java/com/royalpearl/hotel/
    │   │   ├── HotelApplication.java          ← entry point
    │   │   ├── common/                        ← ApiResponse, ReferenceGenerator
    │   │   ├── config/                        ← SecurityConfig, JpaConfig, OpenApiConfig,
    │   │   │                                     AdminAllowlistProperties, OAuth2Config
    │   │   ├── exception/                     ← GlobalExceptionHandler (RFC 7807),
    │   │   │                                     ResourceNotFoundException, ConflictException,
    │   │   │                                     BadRequestException
    │   │   ├── security/                      ← JwtService, JwtProperties,
    │   │   │                                     JwtAuthenticationFilter, HotelUserDetails,
    │   │   │                                     HotelUserDetailsService, SecurityUtil
    │   │   ├── auth/                          ← entity/RefreshToken, repository, service,
    │   │   │                                     controller, dto
    │   │   ├── user/                          ← entity/{User,UserRole,AppRole},
    │   │   │                                     repository, service, controller, dto, mapper
    │   │   ├── room/                          ← entity/Room, repository, service,
    │   │   │                                     controller, dto, mapper
    │   │   ├── booking/                       ← entity/Booking, repository, service,
    │   │   │                                     controller, dto, mapper
    │   │   ├── menu/                          ← entity/MenuItem, repository, service,
    │   │   │                                     controller, dto, mapper
    │   │   ├── order/                         ← entity/{TableReservation,OrderItem},
    │   │   │                                     repository, service, controller, dto, mapper
    │   │   ├── contact/                       ← entity/{ContactMessage,NewsletterSubscriber},
    │   │   │                                     repository, service, controller, dto, mapper
    │   │   └── admin/                         ← service/AdminService, controller/AdminController,
    │   │                                         dto/{AdminStatsDto,RoleRequest}
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── application-test.yml
    │       ├── logback-spring.xml
    │       └── db/migration/
    │           ├── V1__init.sql
    │           └── V2__seed.sql
    └── test/
        └── java/com/royalpearl/hotel/
            ├── AbstractIntegrationTest.java
            ├── auth/AuthFlowIntegrationTest.java
            ├── booking/BookingIntegrationTest.java
            ├── order/OrderIntegrationTest.java
            ├── security/AdminAccessControlTest.java
            ├── security/ReferenceGeneratorTest.java
            └── admin/AdminPaginationTest.java
```

---

## 7. Architecture Decisions

| Decision | Choice | Reason |
|---|---|---|
| Auth | Stateless JWT (HS256) | No server-side session; scales horizontally. Roles in token = no DB hit per request |
| Refresh tokens | Opaque UUID, SHA-256 hashed in DB | Never store raw tokens; rotation on each use |
| Payment status | Server-enforced always `pending` on create | Client field is ignored; only admin PATCH can change it |
| Booking refs | RP + 7 random digits | Human-readable, 9M range, collision-retry loop |
| Order refs | RPO- + 7 random digits | Same rationale |
| JSONB | Hypersistence JsonBinaryType | Room amenities and order items as typed JSONB arrays |
| Roles | Separate `user_roles` table, `app_role` PG enum | Clean RBAC, easy multi-role per user, no role column on users |
| Migrations | Flyway + `validate` DDL mode | Schema owned by SQL, not Hibernate |
| Errors | RFC 7807 `ProblemDetail` | Standard, machine-readable error format |
| CORS | Configurable via `CORS_ALLOWED_ORIGINS` env | No hardcoded frontend origins |
| OAuth2 | Stub/extension point for Google | Wired up; enable by setting `GOOGLE_CLIENT_ID/SECRET` |

---

## 8. API Endpoint Reference

Base path: `/api/v1`

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register with email + password |
| POST | `/auth/login` | Public | Login, returns token pair |
| POST | `/auth/refresh` | Public | Rotate refresh token |
| POST | `/auth/logout` | Optional JWT | Revoke all refresh tokens |

### Rooms

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/rooms` | Public | List all active rooms |
| GET | `/rooms/{slug}` | Public | Get room by slug |

### Menu

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/menu` | Public | List menu; `?category=Biryani&veg=true` |

### Bookings

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/bookings` | Public | Create booking (guest or authenticated) |
| GET | `/bookings/lookup` | Public | `?reference=RP1234567&phone=9…` |

### Food Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/orders` | Public | Place food order |
| GET | `/orders/lookup` | Public | `?reference=RPO-1234567&phone=9…` |

### Contact

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/contact` | Public | Submit contact message |
| POST | `/newsletter` | Public | Newsletter subscription (idempotent) |

### My Account (authenticated)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/me` | JWT | Get own profile |
| PATCH | `/me` | JWT | Update fullName, phone, avatarUrl |
| GET | `/me/bookings` | JWT | Own bookings (paginated) |
| GET | `/me/orders` | JWT | Own food orders (paginated) |
| POST | `/me/claim-admin` | JWT | Claim admin if email on allowlist |

### Admin (ROLE_admin required)

| Method | Path | Description |
|---|---|---|
| GET | `/admin/stats` | Dashboard counts + revenue |
| GET | `/admin/bookings` | Search: `?search=&status=&paymentStatus=&bookingStatus=&from=&to=&page=&size=&sort=` |
| GET | `/admin/bookings/{id}` | Single booking by UUID |
| PATCH | `/admin/bookings/{id}` | Update status / paymentStatus / notes |
| GET | `/admin/orders` | Search orders (same params) |
| GET | `/admin/orders/{id}` | Single order |
| PATCH | `/admin/orders/{id}` | Update order |
| GET | `/admin/messages` | Contact messages: `?status=&search=&page=&size=` |
| PATCH | `/admin/messages/{id}` | Update message status |
| GET | `/admin/users` | List users: `?search=&page=&size=` |
| POST | `/admin/users/{id}/roles` | Add role |
| DELETE | `/admin/users/{id}/roles/{role}` | Remove role |
| GET | `/admin/newsletter` | Newsletter subscribers |

**OpenAPI / Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 9. Sample curl Requests

### Register
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"guest@example.com","password":"Password123!","fullName":"Rahul Sharma","phone":"9876543210"}'
```

### Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"guest@example.com","password":"Password123!"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo $TOKEN
```

### Create a room booking (anonymous)
```bash
curl -s -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":      "Fatima Begum",
    "email":         "fatima@example.com",
    "phone":         "9000000001",
    "roomName":      "Deluxe Room",
    "checkIn":       "2027-03-10",
    "checkOut":      "2027-03-13",
    "guests":        2,
    "paymentMethod": "online"
  }'
# → {"bookingId":"RP4820193","paymentStatus":"pending", ...}
```

### Look up booking status
```bash
curl "http://localhost:8080/api/v1/bookings/lookup?reference=RP4820193&phone=9000000001"
```

### Place a food order (anonymous)
```bash
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "name":        "Syed Basheer",
    "phone":       "8179469535",
    "orderType":   "dine_in",
    "items": [
      {"name":"Hyderabadi Dum Biryani","price":350,"qty":2},
      {"name":"Irani Chai","price":60,"qty":2}
    ],
    "total":         820,
    "paymentMethod": "cash"
  }'
# → {"orderId":"RPO-7710284","paymentStatus":"pending", ...}
```

### Claim admin role
```bash
# Must be logged in as bashamahemood579@gmail.com
curl -s -X POST http://localhost:8080/api/v1/me/claim-admin \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Admin: mark booking paid
```bash
curl -s -X PATCH "http://localhost:8080/api/v1/admin/bookings/{uuid}" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"paymentStatus":"paid"}'
```

### Admin: search by booking reference
```bash
curl -s "http://localhost:8080/api/v1/admin/bookings?search=RP4820193" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# → {"content":[{"bookingId":"RP4820193",...}],"totalElements":1}
```

### Admin: get dashboard stats
```bash
curl -s http://localhost:8080/api/v1/admin/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### List rooms
```bash
curl http://localhost:8080/api/v1/rooms
```

### Filter menu by category
```bash
curl "http://localhost:8080/api/v1/menu?category=Biryani"
curl "http://localhost:8080/api/v1/menu?veg=true"
curl "http://localhost:8080/api/v1/menu?category=Starters&veg=false"
```

### Refresh token
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<raw-refresh-token>"}'
```

---

## 10. Security Model

### JWT Flow
1. Client calls `/auth/login` → receives `accessToken` (15 min) + `refreshToken` (7 days, opaque UUID).
2. All protected requests include `Authorization: Bearer <accessToken>`.
3. The JWT filter validates the token and populates `SecurityContext` from claims — **no database hit**.
4. When the access token expires, call `/auth/refresh` with the refresh token to rotate both tokens.
5. Refresh tokens are **hashed (SHA-256)** before storage. The raw token is never stored.
6. `/auth/logout` revokes all active refresh tokens for the user.
7. A scheduled job at 03:00 daily purges expired tokens from the database.

### Payment Status Protection
- Clients **cannot** set `paymentStatus` when creating bookings or orders.
- The service layer unconditionally sets it to `"pending"` regardless of any client-supplied value.
- Only an admin calling `PATCH /admin/bookings/{id}` or `PATCH /admin/orders/{id}` can change it.

### Admin Allowlist
- `POST /me/claim-admin` grants the `admin` role only if the authenticated user's email appears in `ADMIN_ALLOWLIST`.
- The allowlist is read from environment/config — never hardcoded in the repository.

### Role Storage
- Roles live in the `user_roles` table, not on the `users` row.
- Spring Security sees `ROLE_admin`, `ROLE_moderator`, `ROLE_user`.
- `@PreAuthorize("hasRole('admin')")` on `AdminController` enforces this at the method level.

### Google OAuth2
- Extension point wired via `OAuth2Config`.
- On successful Google login, the user is upserted (provider=`google`) and assigned `ROLE_user`.
- A JWT access token is issued and the user is redirected to `{FRONTEND_URL}/oauth2/callback?token=…`.
- Enable by setting `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` environment variables.

---

## 11. Acceptance Criteria Checklist

| Criterion | Status |
|---|---|
| `docker compose up` starts Postgres + API; migrations run; Swagger loads at `/swagger-ui.html` | ✅ |
| Anonymous `POST /api/v1/bookings` returns 201 with a unique `RP…` reference persisted in DB | ✅ |
| Anonymous `POST /api/v1/orders` with a cart returns 201 with a unique `RPO-…` reference | ✅ |
| Sending `"paymentStatus":"paid"` from a non-admin is ignored; stored as `pending` | ✅ |
| Non-admin calling any `/admin/**` endpoint gets 403 | ✅ |
| `GET /admin/bookings?search=RP4820193` returns exactly that booking | ✅ |
| Refresh token rotation: second use of same token returns 400 | ✅ |
| User can only see their own bookings at `/me/bookings` | ✅ |
| All list endpoints are paginated | ✅ |
| All errors return `application/problem+json` (RFC 7807) | ✅ |

---

## Hotel Contact Details

| | |
|---|---|
| **Address** | Road No. 12, Banjara Hills, Hyderabad, Telangana, India |
| **Reception** | 24/7 |
| **Restaurant** | 12:00 – 23:30 daily |
| **Check-in** | 14:00 |
| **Check-out** | 12:00 |
| **Phone / WhatsApp** | +91 81794 69535 |
| **Email** | reservations@royalpearlhyderabad.com |
| **UPI** | mahemoodbasha@axl (PhonePe 8179469535) |
