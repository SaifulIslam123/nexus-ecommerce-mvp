# Feature: Forgot Password

## Goal
Let a user request a password reset link by email.

## Endpoint
POST /api/v1/auth/forgot-password (public)

Request:  { "email": "user@example.com" }
Response: 202 { "message": "If the account exists, a reset link has been sent." }

## Behavior
1. Validate email format. Invalid returns 400 (standard validation error).
2. Look up the user by email (case-insensitive).
3. If found and active:
    - Invalidate all earlier unused tokens for that user.
    - Generate a 32-byte SecureRandom token, URL-safe Base64.
    - Store SHA-256 hash, user_id, expires_at (now + 30 min), created_at.
    - Send the reset email via EmailSender.
4. If not found: do nothing, return the same 202 response.
5. Response body, status, and (as far as practical) timing must be identical in both cases.

## Rate limiting (Redis)
- Max 3 requests per email per hour.
- Max 10 requests per IP per hour.
- Exceeded returns 429 with the standard error DTO.
- Limit counts requests, not just successes.

## Email
- Interface `EmailSender`; dev implementation logs the link to console, prod implementation uses SMTP (spring-boot-starter-mail).
- Link: `{app.frontend-url}/reset-password?token={rawToken}`
- Subject: "Reset your Nexus password"
- Body states 30-minute expiry and "ignore if you didn't request this".
- New properties: `app.frontend-url`, SMTP settings, `app.mail.from`. Add all to `.env.example`.

## Acceptance Criteria
- AC1: Registered email returns 202, one token row is created, one email is sent.
- AC2: Unknown email returns the identical 202, with no token and no email.
- AC3: Malformed email returns 400.
- AC4: A second request invalidates the first token.
- AC5: Fourth request within an hour for the same email returns 429.
- AC6: Database holds only the hash, never the raw token.
- AC7: Logs contain no tokens or full emails.
- AC8: Endpoint is reachable without authentication in dev and prod security configs.

## Non-goals
Frontend pages, SMS, admin-initiated resets, CAPTCHA.