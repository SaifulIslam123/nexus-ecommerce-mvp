# JWT Refresh Token — Feature Specification

## Overview

Extend the existing JWT authentication to support refresh tokens. Currently, the app issues a single access token (10-hour TTL) on login. This spec introduces a separate short-lived **access token** and a long-lived **refresh token** so clients can silently re-authenticate without forcing users to log in again.

---

## Goals

- Short-lived access tokens (15 minutes) to reduce exposure window
- Long-lived refresh tokens (7 days) stored server-side for revocation support
- Single `/auth/refresh` endpoint to exchange a refresh token for a new access token
- Logout invalidates both tokens
- Works across both `dev` (in-memory) and `prod` (Redis/MySQL) profiles

---

## Token Design

| | Access Token | Refresh Token |
|---|---|---|
| **Format** | JWT (JJWT, HS256) | Opaque UUID string |
| **TTL** | 15 minutes | 7 days |
| **Storage (client)** | Memory / Authorization header | HttpOnly cookie or secure storage |
| **Storage (server)** | Stateless (blacklist on logout only) | Persisted in DB table |
| **Claims** | `sub` (email), `roles` | None — just a lookup key |
| **Rotation** | Issued fresh on each `/auth/refresh` | Rotated on each use (old one revoked) |

The refresh token is intentionally opaque — it is a random UUID that maps to a DB record, giving full revocation control without JWT complexity.

---

## Database

### New table: `refresh_tokens`

```sql
CREATE TABLE refresh_tokens (
    id           CHAR(36)     NOT NULL PRIMARY KEY,  -- UUID, the token value
    user_id      CHAR(36)     NOT NULL,
    expires_at   DATETIME     NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_rt_user ON refresh_tokens(user_id);
```

- Delivered as a new Flyway migration file (`V6__add_refresh_tokens.sql` or next available version).
- `ON DELETE CASCADE` means tokens are automatically cleaned up if the user is deleted.
- A scheduled job will purge expired + revoked rows to prevent table bloat.

---

## New Config Properties

```properties
app.jwt.access-token-expiration-ms=900000          # 15 minutes
app.jwt.refresh-token-expiration-ms=604800000       # 7 days
```

The existing `app.jwt.expiration-ms` property will be replaced by these two.

---

## API Changes

### `POST /api/v1/auth/login` — Updated Response

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

`LoginResponseDto` gains `refreshToken`, `tokenType`, and `expiresIn` fields. The old `token` field is removed.

### `POST /api/v1/auth/refresh` — New Endpoint

**Request:**
```json
{ "refreshToken": "550e8400-e29b-41d4-a716-446655440000" }
```

**Response (200):**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "a1b2c3d4-...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Error cases:**
- `401` — token not found, already revoked, or expired

On success, the old refresh token is **revoked** in the DB and a brand-new one is issued (token rotation). This limits the damage of a stolen refresh token to a single use.

### `POST /api/v1/auth/logout` — Updated Behavior

**Request:** `Authorization: Bearer <accessToken>` + body `{ "refreshToken": "..." }`

Logout now:
1. Blacklists the access token (existing behavior via `TokenBlacklistService`)
2. Revokes the refresh token in the DB

---

## New Components

### `RefreshToken` entity
JPA entity mapping to `refresh_tokens`. Fields: `id` (UUID as String, PK), `user` (ManyToOne), `expiresAt`, `revoked`, `createdAt`.

### `RefreshTokenRepository`
Spring Data JPA repository. Key queries:
- `findByIdAndRevokedFalse(id)` — lookup valid token
- `revokeAllByUserId(userId)` — bulk revoke on password change / security event
- `deleteByExpiresAtBeforeAndRevokedTrue(cutoff)` — cleanup query for scheduler

### `RefreshTokenService`
Handles token lifecycle:
- `createRefreshToken(user)` — generates UUID, persists record, returns token string
- `rotateRefreshToken(tokenId)` — revokes old record, creates and returns new one
- `revokeToken(tokenId)` — marks token revoked
- `revokeAllForUser(userId)` — invalidates all sessions for a user
- `validateRefreshToken(tokenId)` — loads record, checks `revoked=false` and `expiresAt > now()`

### `TokenCleanupScheduler` (or extend existing `OrderScheduler`)
Cron job (e.g., daily at 02:00) that deletes expired and revoked refresh tokens older than their TTL. Keeps the `refresh_tokens` table lean.

---

## Changes to Existing Components

| Component | Change |
|---|---|
| `JwtUtil.kt` | Add `generateAccessToken()` method; rename/deprecate `generateToken()`. Parameterize TTL from the two new config properties. |
| `AuthController.kt` | `/login` returns updated DTO; add `/refresh` endpoint; `/logout` also revokes refresh token |
| `LoginResponseDto.kt` | Add `refreshToken`, `tokenType`, `expiresIn`; remove `token` |
| `application-dev.properties` | Add `access-token-expiration-ms` and `refresh-token-expiration-ms` |
| `application-prod.properties` | Same two properties (no fallback for prod secret) |
| `TokenBlacklistService` / impls | No structural change — access token blacklisting stays as-is |

---

## Token Rotation & Security Properties

- **Refresh token rotation**: Every `/auth/refresh` call revokes the presented token and issues a new one. A stolen token can only be used once before the legitimate client's next refresh invalidates it (and vice versa — the attack is detectable).
- **Revocation on logout**: Immediate, server-side. No waiting for expiry.
- **Cascade revocation**: `revokeAllForUser()` lets an admin or a "log out all devices" feature kill every session.
- **No refresh token in JWT**: Keeping the refresh token as an opaque UUID means it has no embedded expiry that could be forged; expiry is enforced by the DB record.

---

## Open Questions for Review

1. **Refresh token transport**: Should the refresh token be returned in the response body (client manages storage) or set as an HttpOnly `Set-Cookie` header (server manages, more XSS-resistant)? Body is simpler for a mobile/SPA API; cookie is safer for browser-first apps.

  response body

2. **`expiresIn` unit**: Seconds (standard OAuth2 convention) or milliseconds? Spec above uses seconds.  

  Seconds	

3. **Cleanup scheduler**: Extend the existing `OrderScheduler` or create a dedicated `TokenCleanupScheduler`?
	
dedicated `TokenCleanupScheduler`


4. **Logout body vs header**: Passing `refreshToken` in the logout request body is simple; an alternative is a dedicated `POST /auth/revoke` endpoint following RFC 7009. Worth aligning to RFC?

request body , RFC no need now
