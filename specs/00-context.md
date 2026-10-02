Project context — authentication & security (one page)

Package layout (key packages)
- `com.ecommerce.mvp.modules.*` — feature modules (auth, user, product, order, cart, role, etc.)
- `com.ecommerce.mvp.security` — security wiring, JWT util, filters, token blacklist implementations
- `com.ecommerce.mvp.common` — shared config, cache/Redis, exception handling, response DTOs
- `com.ecommerce.mvp.schedulers` — background jobs (e.g. refresh-token cleanup)

User entity & repository
- `modules/user/model/entity/User.kt`: fields: `name`, `email` (unique), `password`, `phone`; relations: `userRoles` (ManyToMany), `orders`, `addresses`, `cart`.
- `modules/user/repository/UserRepository.kt`: JPA repository with `findByUserEmail`, `findByUserEmailWithAddresses`, `existsByEmail` and a couple of address-aware queries.

Auth controller & services
- `modules/auth/controller/AuthController.kt` (`/api/v1/auth`): endpoints
  - `POST /login` — authenticates via `AuthenticationManager` then delegates to `AuthService.login`
  - `POST /register` — encodes password with `BCryptPasswordEncoder` then calls `UserService.registerUser`
  - `POST /refresh` — calls `AuthService.refresh`
  - `POST /logout` — blacklists the JWT and revokes the refresh token
- `modules/auth/service/AuthService.kt`: builds access token (via `JwtUtil`) using roles from authenticated principal, asks `RefreshTokenService` to create refresh tokens, returns `LoginResponseDto`.
- `modules/auth/service/RefreshTokenService.kt`: creates, rotates and revokes refresh tokens persisted in DB. Rotation enforces single-use (old token revoked; new token persisted).

Password encoder bean
- `security/SecurityConfig.kt` exposes `passwordEncoder()` → `BCryptPasswordEncoder()` and configures a `DaoAuthenticationProvider` wired to `AppUserDetailsService`.

JWT & refresh-token flow
- `security/JwtUtil.kt` — Base64-encoded secret (from `app.jwt.secret`), generates access tokens with `roles` claim and expiration; provides helpers `validateToken`, `extractUserEmail`, `extractRoles` (prefixes roles with `ROLE_`).
- `security/JwtAuthenticationFilter.kt` — reads `Authorization: Bearer <token>`, rejects if blacklisted, validates token, extracts roles and sets Spring `Authentication` (no DB lookup required for roles).
- Refresh tokens are persistent (`modules/auth/model/entity/RefreshToken.kt`) with columns `refresh_token` (36), `user_id`, `expires_at`, `revoked`. Repository methods support rotation, revoke-all for a user, and periodic cleanup.

Redis token blacklist (dev vs prod)
- `security/TokenBlacklistService` interface with two implementations:
  - `InMemoryTokenBlacklistService` (active `dev` profile): `ConcurrentHashMap` keyed by token, scheduled purge runs hourly.
  - `RedisTokenBlacklistService` (active `prod` profile): stores keys `token:blacklist:<jwt>` with TTL equal to token remaining lifetime — suitable for multi-instance deployments.
- `AuthController.logout` uses the `TokenBlacklistService` to blacklist access tokens and `RefreshTokenService.revokeToken` to revoke refresh tokens.

Security configuration & public endpoints
- `security/SecurityConfig.kt` contains shared beans. Two profile-specific filter chains:
  - `DevSecurityConfig` (`dev`): permits Swagger/UI + API-docs (paths obtained from `SwaggerProperties`) and ` /api/v1/auth/**` as public; relaxes frame options for H2 console.
  - `ProdSecurityConfig` (`prod`): permits only ` /api/v1/auth/**`; enforces HSTS and secure headers; wires `RateLimitFilter` before auth filter.
- `application-dev.properties` enables Swagger and provides `swagger.public-paths` used by the dev chain.

Exception handler & error DTOs
- `common/exception/GlobalExceptionHandler.kt`: central REST advice returning `ApiResponse<T>` with `success=false` and readable messages for validation errors, authentication errors, invalid refresh token, rate limits, DB errors, etc.
- DTOs: `common/response/ApiResponse.kt` (generic response wrapper), `common/exception/ErrorResponse.kt` (an alternate error DTO present but not used by the global handler).

Flyway migrations (latest)
- Migrations live under `src/main/resources/db/migration`. Present files: `V1`, `V2`, `V3`, `V4`, `V6`, `V7` — latest is `V7__add_version_to_products.sql` (adds `version` column). `V6__fix_refresh_tokens_schema.sql` aligned refresh token table with `BaseEntityAudit` (id primary key, unique refresh_token).

Relevant config properties
- `src/main/resources/application.properties` sets default profile to `dev`.
- `application-dev.properties` includes defaults for `app.jwt.secret` (a base64 fallback key), `app.jwt.access-token-expiration-ms`, DB dev URL/creds, `spring.cache.type=none` (Redis disabled in dev), and Swagger settings.
- `application-prod.properties` requires env vars for `JWT_SECRET`, DB and Redis settings; enables Redis cache and disables Swagger.
- No `spring.mail.*` properties or mail sender beans detected — there is no built-in mail service in the repository.

Test setup
- `src/test/.../EcommerceApplicationTests.kt` exists but is commented out — there are effectively no active automated tests. `build.gradle.kts` includes JUnit and Spring Boot test dependencies.

Missing / Recommended items
- No email sending integration (e.g., JavaMailSender) or templates — needed for signup confirmation / password reset flows.
- No explicit password-reset / verify-email endpoints or tokens stored for those flows.
- No automated tests for auth flows (unit/integration tests for login/refresh/logout/jwt validation).
- Ensure production `JWT_SECRET` is a 256-bit base64 string and enforced; consider startup-time validation.
- Consider tighter logout semantics: after blacklist lookup fail (Redis unreachable), current services fail-open (log and continue) — decide policy (fail-open vs fail-closed) and document.
- Add monitoring/metrics (login failures, refresh token rotations, blacklist size) and alerts for suspicious activity.
- Consider rate limit configuration visibility (thresholds) and distributed rate-limiting for prod.

Notes
- Refresh tokens are single-use (rotation implemented) and persistent; scheduled cleanup exists (`TokenCleanupScheduler`).
- Blacklist design is appropriate: in-memory for dev, Redis for prod to support multiple instances.

End of context

