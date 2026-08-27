# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run (dev profile — no Redis required)
./gradlew bootRun --args="--spring.profiles.active=dev"

# Run (prod profile — requires Redis and MySQL env vars from .env)
./gradlew bootRun --args="--spring.profiles.active=prod"

# Build JAR
./gradlew clean bootJar

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.ecommerce.mvp.MyTestClass"
```

## Architecture

**Stack:** Spring Boot 3.4 + Kotlin 2.2, MySQL 8 (Flyway migrations), Redis (prod cache/token blacklist), JWT auth via JJWT.

**Package layout:** `com.ecommerce.mvp`
- `modules/<name>/` — one package per domain (auth, user, role, product, category, tag, cart, order, payment, courier, admin, schedulers). Each module contains its controller(s), service, repository, entities, and DTOs.
- `common/` — cross-cutting infrastructure: `config/`, `entity/` (base entities), `exception/` (global handler), `response/` (API wrapper), `cache/`, `seeder/`
- `security/` — JWT filter, security configs, token blacklist (lives at the top-level `com.ecommerce.mvp.security`, not inside `common/`)

**Request flow:** HTTP → `JwtAuthenticationFilter` → Controller → Service → Repository → Entity

## Security & Auth

**Access tokens** (JWT, HS256): embed user email and roles in claims — no DB round-trip on validation. Lifetime: 15 min (configurable). On logout, blacklisted in-memory (dev) or Redis (prod) via `TokenBlacklistService`.

**Refresh tokens** (opaque UUID): stored in the `refresh_tokens` table. Lifetime: 7 days. Each `/auth/refresh` call rotates the token (old token revoked, new one issued). `RefreshTokenService` handles creation, rotation, and revocation. `TokenCleanupScheduler` purges expired/revoked tokens daily at 02:00.

Auth endpoints:
- `POST /api/v1/auth/login` → returns `accessToken` + `refreshToken`
- `POST /api/v1/auth/refresh` → validates opaque token, issues new pair
- `POST /api/v1/auth/logout` → blacklists access token + revokes refresh token

Profile-specific filter chains live in `DevSecurityConfig.kt` and `ProdSecurityConfig.kt`. Shared beans (password encoder, auth provider) are in `SecurityConfig.kt`.

Roles: `ADMIN`, `COURIER`, `SHOPPER` — stored as `ROLE_<name>` in Spring Security context. (`USER` was renamed to `SHOPPER` and `MODERATOR` removed in V4 migration.)

## API Conventions

All routes are prefixed `/api/v1/`. Responses are wrapped by `ResponseWrapperAdvice` into:
```json
{ "success": true, "message": "...", "data": {...}, "timestamp": "..." }
```
Exceptions are handled globally in `GlobalExceptionHandler.kt` — use `ResourceNotFoundException` or `BusinessValidationException` for domain errors.

## Profiles

| Profile | DB | Redis | Swagger | SQL logging |
|---------|-----|-------|---------|-------------|
| `dev`   | local MySQL | disabled (in-memory cache) | `/swagger-ui` | enabled |
| `prod`  | env-var configured | required | disabled | disabled |

Env vars for prod are documented in `.env.example` — copy to `.env` (never commit it).

## Database

Flyway migrations in `src/main/resources/db/migration/`. JPA is set to `validate` — schema changes must go through a new migration file. All entities extend `BaseEntity` (auto-increment `Long` id) or `BaseEntityAudit` (adds `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate` via `AuditorAwareImpl`). Exception: `RefreshToken` uses a `String` UUID primary key.

## Caching

Cache names are centralized in `CacheNames.kt`. In prod, Redis keys are prefixed `ecommerce:`. TTLs: Products 10 min, Product Search 5 min, Categories/Tags 30 min.

## Order Status Flow

`TO_PAY` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `OUT_FOR_DELIVERY` → `DELIVERED` → `RECEIVED`

Alternate terminal states: `CANCELLED`, `FAILED`, `RETURNED`, `REFUNDED`.

Admin order updates go through `AdminOrderController`; shopper-facing order actions through `ShopperOrderController`. Both delegate to `OrderService`.

## Key Implementation Details

- **Product search** uses JPA `Specification` (`ProductSpecification.kt`) supporting keyword, category, price range, tag (AND semantics), and dynamic attribute filters.
- **Category** is a self-referential tree (parent/children). `CategoryController` exposes both flat and tree-shaped responses.
- **Tag entity** lives in `modules/product/model/entity/Tag.kt` even though `TagController`/`TagService` are in `modules/tag/`.
- **Data seeding** runs via `AppCommandLineRunner` on startup (dev only) — `CategoryTagProductDataSeederService` populates initial catalogue data.
- **Schedulers**: `OrderScheduler` runs daily at midnight — auto-transitions orders older than 15 days from `DELIVERED` → `RECEIVED`. `TokenCleanupScheduler` runs daily at 02:00 — purges expired and revoked refresh tokens from the DB.
- **Login vs request auth**: `AppUserDetailsService` loads the user from DB only during `/auth/login` (Spring Security auth provider). All subsequent requests are validated stateless from the JWT without a DB call.

## Testing

Tests live in `src/test/kotlin/`. The framework (JUnit 5 via `kotlin-test-junit5`) is configured but tests are minimal — the context-load test in `EcommerceApplicationTests.kt` is currently commented out.
