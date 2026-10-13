# CLAUDE.md

Guidance for Claude Code when working in this repository. Keep changes small, match existing patterns, and ask when a spec is ambiguous.

## Project

Nexus e-commerce MVP: a Spring Boot REST API (Kotlin) with JWT auth, role-based access (SHOPPER / ADMIN / COURIER), product catalogue, cart, orders, payments, couriers, and a password-reset flow.

- **Stack:** Spring Boot 3.4.11, Kotlin 2.2.0, JDK 17 toolchain, Gradle (Kotlin DSL, wrapper 8.11.1)
- **Data:** MySQL 8 + Flyway, Spring Data JPA (`ddl-auto=validate`), Redis (prod only: cache, token blacklist, rate limiting)
- **Auth:** JJWT 0.11.5 (HS256), BCrypt, Spring Security with `@EnableMethodSecurity`
- **Docs:** springdoc OpenAPI (dev only)
- Base package: `com.ecommerce.mvp`

## Read first

1. `specs/constitution.md` — non-negotiable rules. Follow strictly.
2. `specs/00-context.md` — auth/security architecture overview. **Partly outdated**: it says there is no mail service and migrations stop at V7. Trust the code over it.
3. Feature work is spec-driven: `specs/<feature>.md` → `specs/<feature>-plan.md` → `specs/<feature>-tasks.md` (see the forgot/reset-password files for the pattern). Older specs/steering notes live in `.claude/`.

## Commands

```bash
./gradlew bootRun                                   # dev profile is the default
./gradlew bootRun --args="--spring.profiles.active=prod"   # needs env vars from .env.example
./gradlew build                                     # compile + tests; run before declaring a task done
./gradlew test
./gradlew test --tests "com.ecommerce.mvp.SomeTest"
./gradlew clean bootJar
./gradlew flywayInfo                                # Flyway Gradle plugin; reads DB_URL / DB_USERNAME / DB_PASSWORD / DB_SCHEMA
```

Local dev needs MySQL on `localhost:3306` with schema `ecommerce_dev` (credentials in `application-dev.properties`). Redis is not needed for cache in dev (`spring.cache.type=none`).

**Never run `flywayClean`** (clean is enabled in `build.gradle.kts`) and never point Flyway tasks at a non-local database.

## Architecture

```
com.ecommerce.mvp
├── modules/<name>/   admin, auth, cart, category, courier, order, payment, product, role, tag, user
│                     each: controller(s), service/, repository/, model/entity/, model/dto/
├── common/           cache/, config/, dto/, email/, entity/, exception/, response/, seeder/
├── security/         JWT filter/util, Dev/Prod filter chains, rate limiting, token blacklist, @IsAdmin etc.
└── schedulers/       OrderScheduler, TokenCleanupScheduler
```

Flow: HTTP → (prod: `RateLimitFilter`) → `JwtAuthenticationFilter` → Controller → Service → Repository → Entity.

Conventions observed in the code:
- Controllers return **plain DTOs**; `ResponseWrapperAdvice` wraps them into `ApiResponse(success, message, data, timestamp)`. To set a status code (e.g. 202), return `ResponseEntity<ApiResponse<Unit>>` — already-wrapped bodies are not double-wrapped.
- Never return entities from endpoints. Map with `toDto()` / `toEntity()` extension functions in the DTO files.
- Request validation: Jakarta annotations on DTOs with `@field:` use-site targets and `@Valid` on `@RequestBody`.
- Entities extend `BaseEntity` (`Long` IDENTITY id) or `BaseEntityAudit` (adds `createdDate/modifiedDate/createdBy/modifiedBy`; `createdBy` comes from the SecurityContext via `AuditorAwareImpl`). `RefreshToken` is the exception with a String UUID key. The `allOpen` and `lombok` Kotlin plugins are configured; entities are no-arg-constructor classes with `var` fields.
- Repositories are Spring Data interfaces; `ProductSpecification` builds dynamic product search (keyword, category, price range, tags, attributes). List endpoints should paginate in the database, not in memory.
- Exceptions: throw `ResourceNotFoundException`, `BusinessValidationException`, `ResourceAlreadyExistException`, `InvalidRefreshTokenException`, `PaymentFailedException`, `RateLimitException`, or `InvalidPasswordResetTokenException`. They are defined at the bottom of `GlobalExceptionHandler.kt` (the last one has its own file) and mapped there. Add a handler there for any new exception type.
- Cache names live in `common/cache/CacheNames.kt`; TTLs are set per cache in `RedisConfig.kt` (products 10 min, product search 5 min, recommendations 10 min, categories/tags 30 min). Redis caching is `@Profile("prod")`.

## Security and auth

- Public routes: `/api/v1/auth/**` only (dev additionally permits Swagger paths from `swagger.public-paths`). Everything else needs a valid JWT.
- Authorization is by annotation: `@IsAdmin`, `@IsShopper`, `@IsCourier` (in `security/Role.kt`, wrapping `@PreAuthorize`). Put them on the class or the method. **Every new endpoint must have an explicit access decision** — either public by design or one of these annotations.
- Roles are `ERole { SHOPPER, ADMIN, COURIER }`, stored as `ROLE_<name>` in the security context.
- Access tokens: JWT with email + roles in claims, 15 min default; no DB lookup per request. Logout blacklists the token (`InMemoryTokenBlacklistService` in dev, `RedisTokenBlacklistService` in prod).
- Refresh tokens: opaque UUID in `refresh_tokens`, 7 days, rotated on every `/auth/refresh`; `RefreshTokenService.revokeAllForUser` is used after a password reset.
- Password reset (`PasswordResetService`): 256-bit random token, only the SHA-256 hash is stored in `password_reset_tokens`, 30-minute TTL, single use, older unused tokens are invalidated on a new request. `forgot-password` always returns 202 with the same message whether or not the account exists. Email goes through the `EmailSender` interface: `DevEmailSender` (dev, logs a masked address and a redacted link) and `SmtpEmailSender` (prod).
- Rate limiting is Redis-based (`RateLimitFilter`, `RateLimitService`, limits in `RateLimitConstants`): 5/min per IP on login and register, forgot-password 3/hour per email and 10/hour per IP, 100/min per authenticated user, 30/min per IP for public routes. The filter is wired into the **prod** chain only.
- `AppUserDetailsService` hits the DB only during `/auth/login`.

Hard rules from `specs/constitution.md` (summarised, the file is authoritative):
- Never log tokens, passwords, or full email addresses.
- Never return stack traces or raw `ex.message` from unexpected or framework exceptions in API responses.
- Store only hashed reset tokens; responses must not reveal whether an account exists.
- Schema changes only through **new** Flyway migrations — never edit an existing one.
- Secrets and config come from environment properties; document new variables in `.env.example`. Never commit `.env`.
- Every endpoint is documented with OpenAPI annotations and returns the `ApiResponse` structure.
- No unrelated refactors.

## Database and migrations

- Migrations: `src/main/resources/db/migration/V<n>__<description>.sql`. Current files are V1–V4 and V6–V8 (there is no V5). **The next migration is `V9`.** Prod has `out-of-order=false`, so do not backfill a V5.
- Hibernate only validates the schema, so every entity change needs a matching migration, and the entity field names/types must match the columns.
- Follow the style of `V8__create_password_reset_tokens.sql` for new tables (audit columns for `BaseEntityAudit`).
- Product uses optimistic locking (`@Version`, added in V7); stock is decremented with the atomic `CartItemRepository.decrementStockIfAvailable` query (`WHERE stock >= :qty`). Keep that approach for inventory changes.

## Profiles and config

| | dev (default) | prod |
|---|---|---|
| DB | local MySQL `ecommerce_dev`, creds in `application-dev.properties` | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SCHEMA` |
| Redis | not used for cache | required: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_SSL_ENABLED` |
| JWT secret | fallback value in `application-dev.properties` | `JWT_SECRET` required (Base64, 256-bit), no fallback |
| Swagger | `/swagger-ui`, `/v3/api-docs` | disabled |
| Email | `DevEmailSender` (logs only) | SMTP via `MAIL_*`; links built from `FRONTEND_URL`, sender from `MAIL_FROM` |
| Logging | DEBUG for the app, SQL and bind params shown | INFO / WARN, SQL off |
| Extras | | actuator on management port (`MANAGEMENT_PORT`), HSTS, Hikari tuning |

Do not copy dev credentials or the dev JWT fallback into prod config.

## Orders and background jobs

- `OrderStatus`: `TO_PAY → CONFIRMED → PROCESSING → SHIPPED → OUT_FOR_DELIVERY → DELIVERED → RECEIVED`, with terminal states `CANCELLED`, `FAILED`, `RETURNED`, `REFUNDED`. Check `Order.kt` for the authoritative list before touching transitions.
- Admin order actions: `AdminOrderController` (`/api/v1/admin/orders`). Shopper actions: `ShopperOrderController` (`/api/v1/shopper/orders`). Courier actions: `CourierOrderController` (`/api/v1/courier/orders`).
- `OrderScheduler` (daily 00:00) moves `DELIVERED` orders older than 15 days to `RECEIVED`. `TokenCleanupScheduler` (daily 02:00) purges expired/revoked refresh tokens and expired/used password-reset tokens.

## Testing

- JUnit 5 + Spring Boot Test are on the classpath, but `EcommerceApplicationTests.kt` is entirely commented out, so there is effectively **no test coverage** and no test database setup (no H2 or Testcontainers dependency).
- When adding tests, prefer unit tests for services (mock repositories) and `@WebMvcTest` / MockMvc for controllers. A `@SpringBootTest` needs MySQL, so add a test datasource strategy first and say so.
- Add or update tests for any behaviour you change, especially auth, password reset, rate limiting, and stock/payment logic.

## Known gaps (verify before relying on or "fixing" them)

- The forgot-password rate-limit branch in `RateLimitFilter` reads `request.getParameter("email")`, but the endpoint takes a JSON body, so the per-email key may be empty. The matching check inside `PasswordResetService.forgotPassword` is commented out, and `/auth/reset-password` is commented out of the filter's sensitive list even though its OpenAPI docs promise 10/IP/hour.
- `RateLimitFilter` and `JwtAuthenticationFilter` are `@Component` filters without `@Profile`; Spring Boot may register them as servlet filters in dev too. If dev requests fail on Redis connections, start there.
- `AppCommandLineRunner` is entirely commented out; the seeder only runs if you re-enable it, and there is no active seeding.
- `00-context.md`, `.github/copilot-instructions.md` (which tells Copilot to ignore this file), `HELP.md` (Maven boilerplate) and `goodtohave.txt` are stale or scratch files.
- No CI pipeline, no README, no CORS configuration, no release tags.

## Working agreements

- Work only on the task given; do not refactor unrelated code or reformat files you are not changing.
- Read the surrounding module before adding code and mirror its naming, package layout, and DTO/mapper style.
- Run `./gradlew build` before declaring a task done, and report anything you could not run.
- Do not commit secrets, `.env`, or real credentials. Do not edit existing Flyway migrations.
- Prefer a short plan and a small diff. If a spec is ambiguous or contradicts the code, stop and ask.