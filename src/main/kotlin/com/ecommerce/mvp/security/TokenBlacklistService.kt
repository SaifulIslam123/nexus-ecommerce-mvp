package com.ecommerce.mvp.security

/**
 * Contract for invalidating JWT tokens on logout.
 *
 * Two implementations exist:
 *  - [InMemoryTokenBlacklistService]  — active in the `dev` profile (no Redis required)
 *  - [RedisTokenBlacklistService]     — active in the `prod` profile (Redis-backed, TTL auto-expires entries)
 */
interface TokenBlacklistService {

    /**
     * Invalidates [token] so that [isBlacklisted] returns `true` for
     * every subsequent request until the token's natural expiration.
     */
    fun blacklist(token: String)

    /** Returns `true` when the token has been explicitly invalidated via logout. */
    fun isBlacklisted(token: String): Boolean
}
