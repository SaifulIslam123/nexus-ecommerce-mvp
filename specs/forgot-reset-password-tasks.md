# Implementation Tasks: Forgot Password & Reset Password Features

## Task 1: Create Exception Class for Invalid Password Reset Token

**Description:** Create a new exception class to represent invalid/expired/used password reset tokens.

**Files to Create:**
- `src/main/kotlin/com/ecommerce/mvp/common/exception/InvalidPasswordResetTokenException.kt`

**Files to Modify:**
- None (handled by GlobalExceptionHandler in Task 10)

**Tests Required:**
- None (exception class is trivial)

**Done Check:**
- [ ] File created with class extending `RuntimeException`
- [ ] Exception can be thrown and caught
- [ ] Constructor accepts message parameter

---

## Task 2: Create Flyway Migration for password_reset_tokens Table

**Description:** Create database schema for storing password reset tokens following the `BaseEntityAudit` pattern.

**Files to Create:**
- `src/main/resources/db/migration/V8__create_password_reset_tokens.sql`

**Files to Modify:**
- None

**Tests Required:**
- Manual: Run `./gradlew build` and verify Flyway migration completes without errors
- Manual: Verify table structure in dev database:
  - Check `password_reset_tokens` table exists
  - Verify columns: `id`, `token_hash` (unique), `user_id` (FK), `expires_at`, `used_at`, `created_date`, `modified_date`, `created_by`, `modified_by`
  - Verify indexes on `token_hash` and `user_id`

**Done Check:**
- [ ] Migration file V8 created in correct location
- [ ] SQL creates `password_reset_tokens` table with all required columns
- [ ] `token_hash` is VARCHAR 64 and unique
- [ ] `user_id` has FK constraint to users with CASCADE delete
- [ ] Audit columns present (created_date, modified_date, created_by, modified_by)
- [ ] Flyway applies migration successfully on `./gradlew build`

---

## Task 3: Create PasswordResetToken Entity

**Description:** Create JPA entity extending `BaseEntityAudit` for password reset tokens.

**Files to Create:**
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/model/entity/PasswordResetToken.kt`

**Files to Modify:**
- None

**Tests Required:**
- Unit: Verify entity fields are correctly mapped (tokenHash, user, expiresAt, usedAt)
- Unit: Verify relations (ManyToOne to User)
- Integration: Create and persist a PasswordResetToken, verify it's saved with audit fields

**Done Check:**
- [ ] Entity extends `BaseEntityAudit`
- [ ] Fields mapped: `tokenHash` (VARCHAR 64, unique, non-null), `user` (ManyToOne to User), `expiresAt` (Instant, non-null), `usedAt` (Instant, nullable)
- [ ] Proper JPA annotations (Column, ManyToOne, JoinColumn)
- [ ] Inherits audit fields from BaseEntityAudit
- [ ] Entity compiles without errors

---

## Task 4: Create PasswordResetTokenRepository

**Description:** Create JPA repository with custom query methods for password reset token operations.

**Files to Create:**
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/repository/PasswordResetTokenRepository.kt`

**Files to Modify:**
- None

**Tests Required:**
- Integration: Test `findByTokenHashAndUsedAtNull()` returns token when unused, returns empty when used
- Integration: Test `findByUserIdAndUsedAtNull()` returns all unused tokens for a user
- Integration: Test `markAllUserTokensAsUsed()` sets used_at for all user's unused tokens
- Integration: Test `deleteExpiredAndUsedTokens()` removes expired or used tokens

**Done Check:**
- [ ] Repository extends `JpaRepository<PasswordResetToken, Long>`
- [ ] Method `findByTokenHashAndUsedAtNull(hash: String): Optional<PasswordResetToken>` exists
- [ ] Method `findByUserIdAndUsedAtNull(userId: Long): List<PasswordResetToken>` exists
- [ ] Method `markAllUserTokensAsUsed(userId: Long, now: Instant)` with @Modifying @Transactional exists
- [ ] Method `deleteExpiredAndUsedTokens(now: Instant)` with @Modifying @Transactional exists
- [ ] All finder methods tested and passing

---

## Task 5: Create Request DTOs for Forgot & Reset Password

**Description:** Create request DTOs with validation for both endpoints.

**Files to Create:**
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/model/ForgotPasswordRequest.kt`
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/model/ResetPasswordRequest.kt`

**Files to Modify:**
- None

**Tests Required:**
- Unit: Test ForgotPasswordRequest validation (email required, valid format)
- Unit: Test ResetPasswordRequest validation (token required, newPassword required)
- Integration: Test endpoint rejects malformed requests with 400

**Done Check:**
- [ ] `ForgotPasswordRequest` has `email` field with @Email @NotBlank annotations
- [ ] `ResetPasswordRequest` has `token` @NotBlank and `newPassword` @NotBlank fields
- [ ] Both DTOs use `ApiResponse<Unit>` pattern for responses
- [ ] Invalid requests produce standard validation error messages

---

## Task 6: Create EmailSender Interface and Implementations

**Description:** Create email service abstraction with dev (logs) and prod (SMTP) implementations.

**Files to Create:**
- `src/main/kotlin/com/ecommerce/mvp/common/email/EmailSender.kt` (interface)
- `src/main/kotlin/com/ecommerce/mvp/common/email/DevEmailSender.kt` (dev implementation, @Profile("dev"))
- `src/main/kotlin/com/ecommerce/mvp/common/email/SmtpEmailSender.kt` (prod implementation, @Profile("prod"))

**Files to Modify:**
- None

**Tests Required:**
- Unit: Test DevEmailSender logs reset link to console (capture logs, verify message contains token and subject)
- Integration (dev): Verify logs do NOT contain full raw token or passwords, only link preview
- Unit: Mock JavaMailSender for SmtpEmailSender, verify email properties set correctly
- Integration (prod): Verify SmtpEmailSender calls JavaMailSender.send() with correct Message

**Done Check:**
- [ ] `EmailSender` interface has method `sendPasswordResetEmail(userEmail: String, rawToken: String, frontendUrl: String)`
- [ ] `DevEmailSender` (@Profile("dev")) logs link and includes subject, expiry message, and ignore warning
- [ ] `SmtpEmailSender` (@Profile("prod")) uses `JavaMailSender` from Spring
- [ ] Dev implementation does NOT log raw token or full email, only redacted link
- [ ] Prod implementation reads `app.mail.from` property
- [ ] Both implementations are `@Component` and autowired correctly

---

## Task 7: Update Configuration Properties

**Description:** Add email and password reset properties to configuration files.

**Files to Modify:**
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`
- `.env.example`

**Tests Required:**
- Integration: Verify properties are loaded correctly in both dev and prod profiles
- Manual: Check `.env.example` is up-to-date

**Done Check:**
- [ ] `app.frontend-url` property added with dev default `http://localhost:3000`
- [ ] `app.mail.from` property added with dev default `noreply@nexus.ecommerce`
- [ ] `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password` in application-prod.properties
- [ ] SMTP properties configured for authentication and TLS
- [ ] `.env.example` updated with all new variables documented
- [ ] Dev properties have sensible defaults; prod requires env vars

---

## Task 8: Enhance RateLimitService for Forgot Password

**Description:** Add specialized rate limiting methods for forgot-password (per-email + per-IP dual limits).

**Files to Modify:**
- `src/main/kotlin/com/ecommerce/mvp/security/RateLimitService.kt`

**Files to Create:**
- None

**Tests Required:**
- Unit: Test `isForgotPasswordAllowed(email: String, ip: String, emailLimit: Long, ipLimit: Long): Boolean`
  - Verify returns true when both email and IP counters are under limit
  - Verify returns false when email counter exceeds limit
  - Verify returns false when IP counter exceeds limit
  - Verify returns false when both exceed limits
- Unit: Test counters use different Redis keys (`forgot-password:email:{email}` and `forgot-password:ip:{ip}`)
- Unit: Test TTL is 3600 seconds (1 hour) for both counters

**Done Check:**
- [ ] New method `isForgotPasswordAllowed(email: String, ip: String): Boolean` added
- [ ] Method checks email rate limit (3 per hour) with Redis key `rate_limit:forgot-password:email:{email}`
- [ ] Method checks IP rate limit (10 per hour) with Redis key `rate_limit:forgot-password:ip:{ip}`
- [ ] Returns false if either limit exceeded
- [ ] Both counters auto-increment and expire after 3600 seconds
- [ ] Unit tests pass

---

## Task 9: Create PasswordResetService

**Description:** Implement core business logic for forgot-password and reset-password flows.

**Files to Create:**
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/service/PasswordResetService.kt`

**Files to Modify:**
- None

**Tests Required:**
- Integration: Test `forgotPassword(email, ip)` for registered user
  - Verify PasswordResetToken created with correct hash
  - Verify token persisted in DB
  - Verify email sent
  - Verify all earlier unused tokens for user are marked used
  - Verify response is 202
- Integration: Test `forgotPassword(email, ip)` for unknown email
  - Verify NO token created
  - Verify NO email sent
  - Verify response is still 202 (timing-safe)
  - Verify rate limit counted
- Integration: Test `forgotPassword(email, ip)` rate limiting
  - 3 requests per email per hour, 10 per IP per hour
  - 4th request for same email returns 429
  - 11th request for same IP returns 429
- Integration: Test `resetPassword(token, newPassword)` with valid token
  - Verify user password updated and hashed
  - Verify token marked used (used_at set)
  - Verify refresh tokens revoked
  - Verify access tokens blacklisted
  - Verify confirmation email sent
  - Verify response is 200
- Integration: Test `resetPassword(token, newPassword)` with used token
  - Verify returns generic 400, no error details
  - Verify user password NOT updated
  - Verify token still marked used
- Integration: Test `resetPassword(token, newPassword)` with expired token
  - Verify returns generic 400
  - Verify user password NOT updated
- Integration: Test `resetPassword(token, newPassword)` with invalid password
  - Verify returns 400 with validation details
  - Verify token still usable
  - Verify no password update
- Integration: Test `resetPassword()` concurrent requests with same token
  - Thread 1 and Thread 2 both call with same token simultaneously
  - Verify exactly one succeeds (200), one fails (400)
- Unit: Verify token generation uses SecureRandom, 32 bytes, Base64-URL-safe
- Unit: Verify token hashing uses SHA-256

**Done Check:**
- [ ] Service has `forgotPassword(email: String, clientIp: String): ResponseEntity`
- [ ] Service has `resetPassword(token: String, newPassword: String): ResponseEntity`
- [ ] Forgot-password validates email format, checks rate limits (per-email & per-IP), handles both found and not-found users
- [ ] Forgot-password invalidates previous tokens and sends email only for found users
- [ ] Forgot-password always returns 202 with identical message
- [ ] Reset-password validates token hash, checks expiry and used_at, updates password atomically
- [ ] Reset-password revokes refresh tokens and blacklists access tokens on success
- [ ] Reset-password returns generic 400 for all token failures (unknown/expired/used)
- [ ] Reset-password rate limited per IP (10/hour)
- [ ] All sensitive data (tokens, passwords) NOT logged
- [ ] Concurrent token reuse prevented (one 200, one 400)
- [ ] Integration tests passing

---

## Task 10: Add Endpoints to AuthController

**Description:** Add forgot-password and reset-password endpoints to the controller.

**Files to Modify:**
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/controller/AuthController.kt`

**Files to Create:**
- None

**Tests Required:**
- Integration: Test `POST /api/v1/auth/forgot-password` with valid email
  - Verify 202 response
  - Verify service called
- Integration: Test `POST /api/v1/auth/forgot-password` with invalid email
  - Verify 400 with validation error
- Integration: Test `POST /api/v1/auth/reset-password` with valid token and password
  - Verify 200 response
  - Verify service called
- Integration: Test `POST /api/v1/auth/reset-password` with invalid token
  - Verify 400 with generic message
- Integration: Test `POST /api/v1/auth/reset-password` with missing fields
  - Verify 400 with validation error

**Done Check:**
- [ ] Method `@PostMapping("/forgot-password")` added to AuthController
- [ ] Method accepts `ForgotPasswordRequest` with @Valid
- [ ] Method calls `PasswordResetService.forgotPassword(email, clientIp)`
- [ ] Method returns `ResponseEntity` with 202 status
- [ ] Method `@PostMapping("/reset-password")` added to AuthController
- [ ] Method accepts `ResetPasswordRequest` with @Valid
- [ ] Method calls `PasswordResetService.resetPassword(token, newPassword)`
- [ ] Method returns `ResponseEntity` with 200 or 400 status
- [ ] Both endpoints extract client IP from request (X-Forwarded-For or remoteAddr)
- [ ] Endpoints are public (/api/v1/auth/** already permitted)
- [ ] Integration tests passing

---

## Task 11: Update GlobalExceptionHandler for PasswordResetTokenException

**Description:** Add exception handler for InvalidPasswordResetTokenException to return generic 400 response.

**Files to Modify:**
- `src/main/kotlin/com/ecommerce/mvp/common/exception/GlobalExceptionHandler.kt`

**Files to Create:**
- None

**Tests Required:**
- Unit: Test handler returns 400 with generic message "Invalid or expired token"
- Unit: Verify handler does NOT expose exception message or details
- Integration: Throw exception from service, verify handler catches and returns correct response

**Done Check:**
- [ ] Exception handler method added for `InvalidPasswordResetTokenException`
- [ ] Returns `ApiResponse<Unit>` with `success=false`
- [ ] Returns 400 (BAD_REQUEST) status
- [ ] Message is generic: "Invalid or expired token" (no details about unknown vs expired vs used)
- [ ] Exception message or stack trace NOT exposed in response
- [ ] Handler registered with @ExceptionHandler and @ResponseStatus
- [ ] Unit tests passing

---

## Task 12: Update TokenCleanupScheduler for Password Reset Tokens

**Description:** Extend existing scheduler to also clean expired/used password reset tokens.

**Files to Modify:**
- `src/main/kotlin/com/ecommerce/mvp/schedulers/TokenCleanupScheduler.kt`

**Files to Create:**
- None

**Tests Required:**
- Integration: Verify scheduler deletes expired password reset tokens (expiresAt < now)
- Integration: Verify scheduler deletes used password reset tokens (used_at != null)
- Integration: Verify scheduler runs on configured schedule (e.g., hourly)

**Done Check:**
- [ ] Scheduler autowires `PasswordResetTokenRepository`
- [ ] Scheduled method calls `repository.deleteExpiredAndUsedTokens(Instant.now())`
- [ ] Cleanup runs at same schedule as refresh token cleanup (e.g., every hour)
- [ ] Logging includes count of deleted tokens
- [ ] Integration test verifies old tokens are cleaned up

---

## Task 13: Update RateLimitFilter to Reference Reset-Password Endpoint

**Description:** Verify reset-password endpoint is already in sensitiveEndpoints list and rate-limits correctly.

**Files to Modify:**
- `src/main/kotlin/com/ecommerce/mvp/security/RateLimitFilter.kt` (verify only, no changes needed)

**Files to Create:**
- None

**Tests Required:**
- Integration: Verify `/api/v1/auth/reset-password` is in `sensitiveEndpoints` list
- Integration: Test reset-password is rate-limited per IP (10 req/min from RateLimitConstants.SENSITIVE_LIMIT)
- **Note:** Task 8 handles the forgot-password dual rate limit override

**Done Check:**
- [ ] `/api/v1/auth/reset-password` already in sensitiveEndpoints (line 29)
- [ ] Reset-password endpoint uses SENSITIVE_LIMIT (5/min per IP) for token-guessing protection
- [ ] Rate limit filter does NOT apply forgot-password dual limit (handled in PasswordResetService)
- [ ] Integration tests verify rate limiting works

---

## Task 14: Create Integration Tests for Full Forgot Password Flow

**Description:** Test complete forgot-password flow end-to-end.

**Files to Create:**
- `src/test/kotlin/com/ecommerce/mvp/modules/auth/ForgotPasswordIntegrationTests.kt`

**Files to Modify:**
- None

**Tests Required:**
- Test registered user receives 202 and token is created
- Test unknown email receives 202 with no token/email
- Test malformed email returns 400
- Test second request invalidates first token
- Test 4th request in same hour returns 429 for email
- Test 11th request in same hour returns 429 for IP
- Test email body contains reset link with correct token
- Test logs do NOT contain raw tokens or full emails

**Done Check:**
- [ ] Test class created with @SpringBootTest
- [ ] Tests are marked @Transactional for isolation
- [ ] Test uses TestRestTemplate or MockMvc
- [ ] All AC1–AC5 from forgot-password spec verified
- [ ] Tests passing with @ActiveProfiles("dev")

---

## Task 15: Create Integration Tests for Full Reset Password Flow

**Description:** Test complete reset-password flow end-to-end.

**Files to Create:**
- `src/test/kotlin/com/ecommerce/mvp/modules/auth/ResetPasswordIntegrationTests.kt`

**Files to Modify:**
- None

**Tests Required:**
- Test valid token and compliant password returns 200
- Test same token used twice: first 200, second 400
- Test expired token returns generic 400
- Test unknown token returns generic 400
- Test weak password returns 400 with validation details
- Test new password equals old password returns 400
- Test user can login with new password after reset
- Test old password no longer works
- Test refresh tokens revoked after reset
- Test access token blacklisted after reset
- Test concurrent requests with same token: one 200, one 400
- Test logs do NOT contain tokens or passwords

**Done Check:**
- [ ] Test class created with @SpringBootTest
- [ ] Tests are marked @Transactional for isolation
- [ ] Test uses TestRestTemplate or MockMvc
- [ ] All AC1–AC10 from reset-password spec verified
- [ ] Test includes concurrent token reuse scenario
- [ ] Tests passing with @ActiveProfiles("dev")

---

## Task 16: Add OpenAPI/Swagger Documentation

**Description:** Document both endpoints in OpenAPI/Swagger.

**Files to Modify:**
- `src/main/kotlin/com/ecommerce/mvp/modules/auth/controller/AuthController.kt` (add annotations)

**Files to Create:**
- None

**Tests Required:**
- Manual: Visit `http://localhost:8080/swagger-ui` and verify endpoints are documented
- Manual: Verify request/response schemas are correct
- Manual: Verify status codes (202, 200, 400, 429) are documented

**Done Check:**
- [ ] Endpoints have @Operation(summary="...", description="...")
- [ ] Endpoints have @ApiResponse annotations for 200, 202, 400, 429 statuses
- [ ] Request DTOs have @Schema annotations on fields
- [ ] Swagger UI displays endpoints correctly
- [ ] Manual verification in dev environment

---

## Task 17: Run Full Gradle Build & Verify No Errors

**Description:** Final integration check that all code compiles and tests pass.

**Files to Modify:**
- None (all previous tasks completed)

**Tests Required:**
- Run `./gradlew build`
- Run `./gradlew test`
- Verify Flyway migration applies
- Verify all 14 integration tests pass

**Done Check:**
- [ ] `./gradlew build` succeeds with no errors
- [ ] `./gradlew test` passes all unit and integration tests
- [ ] Flyway migration V8 applied successfully
- [ ] No compilation warnings or errors
- [ ] All test classes run and pass
- [ ] Code review of all changes

---

## Summary by Phase

**Phase 1: Infrastructure (Tasks 1–2)**
- Exception & migration

**Phase 2: Data Model (Tasks 3–4)**
- Entity and repository

**Phase 3: Configuration & Integration (Tasks 5–8)**
- DTOs, email service, config properties, rate limiting

**Phase 4: Business Logic (Task 9)**
- Core service implementation

**Phase 5: API & Handlers (Tasks 10–13)**
- Controller endpoints, exception handlers, scheduler updates, filter verification

**Phase 6: Testing & Documentation (Tasks 14–16)**
- Integration tests and Swagger docs

**Phase 7: Final Verification (Task 17)**
- Full build and test pass

---

**Estimated Effort:** ~40–60 hours (depending on team experience with Spring Boot and testing)

**Key Success Metrics:**
- All acceptance criteria from specs met
- All integration tests passing
- No sensitive data in logs
- Rate limiting working correctly
- Email service abstraction clean and extensible

