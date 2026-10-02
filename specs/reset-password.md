# Feature: Reset Password

## Goal
Let a user set a new password using a valid reset token.

## Endpoint
POST /api/v1/auth/reset-password (public)

Request:  { "token": "<raw token>", "newPassword": "..." }
Success:  200 { "message": "Password has been reset." }
Failure:  400 { generic "Invalid or expired token" error }

## Behavior
1. Validate the request. A missing token or a password violating policy returns 400 with validation details.
2. Hash the incoming token (SHA-256) and look up the row.
3. Reject with the same generic 400 if the token is unknown, expired, or already used.
4. Encode the new password with the existing PasswordEncoder and update the user.
5. Mark the token used (used_at = now) in the same transaction.
6. Revoke all refresh tokens for the user.
7. Invalidate existing access tokens for the user (Redis blacklist or token-version approach, whichever matches current JWT handling).
8. Reject if the new password equals the current one.
9. Send a "your password was changed" confirmation email. 

## Security
- Never reveal which failure occurred (unknown vs expired vs used).
- Steps 4 and 5 are one transaction. A failure rolls back both.
- Concurrent requests with the same token: exactly one succeeds.
- Rate limit this endpoint per IP (10 per hour) to block token guessing.

## Acceptance Criteria
- AC1: Valid token and compliant password returns 200, hash is updated, used_at is set.
- AC2: The same token used twice returns 400 on the second attempt.
- AC3: Expired token returns the generic 400.
- AC4: Unknown token returns the identical generic 400.
- AC5: Weak password returns 400 with validation details and the token stays usable.
- AC6: After reset, login with the old password fails and the new one succeeds.
- AC7: After reset, previously issued refresh tokens are rejected.
- AC8: After reset, previously issued access tokens are rejected.
- AC9: Two parallel requests with one token: one 200, one 400.
- AC10: Logs contain no tokens or passwords.

## Non-goals
Change-password for logged-in users, password history, MFA.