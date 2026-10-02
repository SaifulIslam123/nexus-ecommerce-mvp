# Plan: Forgot Password & Reset Password Features

## Summary
Implement two password reset features: (1) Forgot Password endpoint allowing users to request a reset link via email with rate limiting per email and IP, and (2) Reset Password endpoint to set a new password using a valid token. This requires a new `PasswordResetToken` entity, email service abstraction, specialized rate limiting overrides, and careful transaction handling to prevent token reuse.

## Files to Create

1. **Entity & Repository**
   - `modules/auth/model/entity/PasswordResetToken.kt` — extends `BaseEntityAudit`; fields: `tokenHash` (hashed SHA-256, unique, indexed), `user` (ManyToOne), `expiresAt`, `createdAt` (inherited), `usedAt` (nullable, set on successful reset)
   - `modules/auth/repository/PasswordResetTokenRepository.kt` — custom finder methods: `findByTokenHashAndUsedAtNull`, `findByUserAndUsedAtNull`, queries for invalidating unused tokens

2. **Email Service**
   - `common/email/EmailSender.kt` — interface with `sendPasswordResetEmail(userEmail, rawToken, frontendUrl)`
   - `common/email/DevEmailSender.kt` — logs link to console (dev profile)
   - `common/email/SmtpEmailSender.kt` — uses `JavaMailSender` (prod profile)

3. **DTOs & Requests**
   - `modules/auth/model/ForgotPasswordRequest.kt` — `{ email: String }`
   - `modules/auth/model/ResetPasswordRequest.kt` — `{ token: String, newPassword: String }`

4. **Services**
   - `modules/auth/service/PasswordResetService.kt` — orchestrates: token generation, hash, DB persist, email send; handles validation, rate-limit checks (per-email per-IP)
   - Extend `RefreshTokenService.revokeAllForUser()` or create helper in auth service to revoke refresh tokens & blacklist access tokens on reset

5. **Controller Endpoints**
   - Add `POST /api/v1/auth/forgot-password` and `POST /api/v1/auth/reset-password` to `AuthController.kt`

6. **Exception Classes**
   - `InvalidPasswordResetTokenException` — for token not found, expired, or already used; caught by global handler returning generic 400

## Flyway Migration

**V8__create_password_reset_tokens.sql** (new version; V7 is latest)
- Create `password_reset_tokens` table with:
  - `id` (auto-increment PK, inherited from BaseEntityAudit pattern)
  - `token_hash` (VARCHAR 64, unique, indexed, never null) — SHA-256 hex
  - `user_id` (bigint, FK → users, ON DELETE CASCADE, indexed)
  - `expires_at` (datetime(6), not null)
  - `used_at` (datetime(6), nullable) — null = unused; set when password reset succeeds
  - Audit columns: `created_date`, `modified_date`, `created_by`, `modified_by`

## Entity & Repository Design

**PasswordResetToken Entity** (extends `BaseEntityAudit`)
- Immutable token storage: only hash stored, never raw token
- `usedAt` distinguishes active vs consumed tokens (atomic update on reset)
- Expires in 30 minutes; cleanup handled by existing `TokenCleanupScheduler` or new scheduled task
- No direct relation to refresh tokens; independent lifecycle

**PasswordResetTokenRepository**
- `findByTokenHashAndUsedAtNull(hash: String): Optional<PasswordResetToken>` — active tokens only
- `findByUserIdAndUsedAtNull(userId: Long): List<PasswordResetToken>` — to invalidate older tokens
- Custom `@Modifying` query to set all unused tokens for a user to a dummy `used_at` on new forget-password request

## Service Flow

**Forgot Password Service (`PasswordResetService.forgotPassword`)**
1. Parse & validate email format → 400 if malformed
2. Case-insensitive user lookup (existing `UserRepository.findByUserEmail`)
3. If user found AND active (not soft-deleted):
   - Invalidate all earlier unused tokens for that user (update `used_at` or create new constraint)
   - Generate 32-byte `SecureRandom` → Base64-URL-safe encode → keep raw token in memory
   - Hash raw token with SHA-256 → hex string
   - Persist new `PasswordResetToken`: hash, user_id, expires_at=now+30min, created_at=now, used_at=null
   - Call `emailSender.sendPasswordResetEmail(email, rawToken, frontendUrl)` with link `{frontendUrl}/reset-password?token={rawToken}`
4. If user not found: do nothing (silent success to prevent enumeration)
5. Rate limits (Redis):
   - Per-email: max 3 per hour
   - Per-IP: max 10 per hour
   - Exceeded → 429
   - Rate limit counts all requests (success & non-found)
6. Response: always 202 `{ message: "If the account exists, a reset link has been sent." }`

**Reset Password Service (`PasswordResetService.resetPassword`)**
1. Validate token and newPassword fields → 400 if missing/invalid
2. Hash incoming token with SHA-256 → lookup by hash
3. If not found, expired, or already used (`used_at != null`): return generic 400 `{ message: "Invalid or expired token" }`
4. Reject if new password equals current hash (compare with BCrypt)
5. Within single transaction:
   - Encode new password with `BCryptPasswordEncoder`
   - Update User.password
   - Set PasswordResetToken.used_at = now
   - Commit
6. On success:
   - Revoke all refresh tokens for user (set `revoked=true`)
   - Blacklist all existing access tokens (add to Redis blacklist or increment token-version in JWT claim)
   - Send confirmation email
7. Rate limit (per-IP): max 10 per hour → 429 if exceeded (protects against token guessing)
8. Response: 200 `{ message: "Password has been reset." }`

## Security Config Changes

**No changes needed to filter chains** — both endpoints already in `sensitiveEndpoints` list in `RateLimitFilter.kt` (lines 28–29), and `/api/v1/auth/**` is already permitted in `DevSecurityConfig.kt` and `ProdSecurityConfig.kt`.

**Rate Limiting Note** — Current `RateLimitConstants.SENSITIVE_LIMIT = 5L` per 60 seconds (5/min) conflicts with specs requiring max 3/hour per email + max 10/hour per IP. Create **per-endpoint override**: new `RateLimitService.isForgotPasswordAllowed(email, ip)` that checks two separate Redis counters with different windows and limits; return 429 if either limit exceeded.

## Config Properties

Add to both `application-dev.properties` and `application-prod.properties`:

```properties
# ── Email / Password Reset ──────────────────────────────────────
app.frontend-url=${FRONTEND_URL:http://localhost:3000}
app.mail.from=${MAIL_FROM:noreply@nexus.ecommerce}

# ── SMTP (prod only) ────────────────────────────────────────────
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

Update `.env.example`:
```dotenv
# ── Email / Password Reset ─────────────────────────────────────
FRONTEND_URL=https://app.nexus.ecommerce
MAIL_FROM=noreply@nexus.ecommerce
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<smtp-user>
MAIL_PASSWORD=<smtp-password>
```

## JWT/Refresh Token Revocation

**On successful password reset:**
1. Call `RefreshTokenService.revokeAllForUser(userId)` — sets `revoked=true` for all user's refresh tokens (atomic operation)
2. Blacklist current access token: add JWT to `TokenBlacklistService` (Redis in prod, in-memory in dev)
3. Optional: increment JWT token-version claim on next login to invalidate all older tokens immediately (requires JwtUtil change)

**Design note:** Current `RefreshTokenService` already has `revokeAllForUser()` method; reuse it.

## Conflicts & Concerns

1. **Rate Limiting Mismatch**
   - **Spec:** 3 req/email/hour + 10 req/IP/hour for forgot-password
   - **Current:** 5 req/min for all sensitive endpoints (login, register, forgot, reset)
   - **Resolution:** Override rate limiter for forgot-password specifically; create per-email + per-IP dual-check logic in `PasswordResetService` or new `ForgotPasswordRateLimitService`

2. **Email Service Not Implemented**
   - Spec assumes `EmailSender` interface + two implementations
   - Current codebase has no mail beans or templates
   - `JavaMailSender` must be added to `build.gradle.kts` for prod (spring-boot-starter-mail)

3. **User.active Field Missing**
   - Specs assume users can be inactive; User entity has no `active` or `deletedAt` field
   - Assumption: all users in DB are active, or implement soft-delete check if needed
   - **Action:** Verify user status check logic with product team if soft-deletes exist

4. **Password Validation Rules**
   - Spec references "password violating policy" but no explicit rules given
   - Current registration accepts any non-blank password
   - **Assumption:** Reuse same validation from registration DTO; clarify min-length, complexity, etc., with team

5. **Concurrent Token Reuse Prevention**
   - Spec: "exactly one succeeds, one 400 on second attempt"
   - **Solution:** Use database `used_at` unique constraint or pessimistic lock; transaction isolation level `SERIALIZABLE` ensures only one UPDATE succeeds; second request re-reads and sees `used_at != null` → 400

6. **Timing Leaks**
   - Spec: "responses identical in timing for found vs not-found user"
   - **Implementation:** Always perform same DB lookup + async email send (fire-and-forget) even if user not found; use `Thread.sleep()` or similar if timing gap remains (not recommended in prod)
   - **Alternative:** Accept minor timing variance; document as acceptable risk

7. **Log Security**
   - Spec forbids logging tokens, passwords, full email addresses
   - Controller/service methods must use `log.debug()` sparingly; redact sensitive data

## Further Considerations

1. **Email Template & Internationalization** — Spec provides basic body text; decide if template files or hardcoded strings are acceptable.
2. **Password History** — Non-goal per spec; old passwords are not checked.
3. **Admin-Initiated Resets** — Non-goal; only user-initiated via email.
4. **CAPTCHA** — Non-goal per spec; rate limiting is the defense against brute force.
5. **Token Customization** — Spec says 32-byte SecureRandom, Base64-URL-safe, 30-min expiry; ensure OpenAPI docs reflect token requirements if exposed in frontend validation.

