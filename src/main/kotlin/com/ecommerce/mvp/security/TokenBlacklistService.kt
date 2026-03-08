package com.ecommerce.mvp.security

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory token blacklist.
 *
 * When a user logs out, their JWT is stored here until it naturally expires.
 * A scheduled task runs every hour to purge already-expired entries and keep
 * memory usage bounded.
 *
 * For a multi-instance deployment, replace the ConcurrentHashMap with a
 * shared Redis cache (e.g. Spring Data Redis + RedisTemplate).
 */
@Service
class TokenBlacklistService(private val jwtUtil: JwtUtil) {

    // token -> expiry time in epoch-millis
    private val blacklist = ConcurrentHashMap<String, Long>()

    /**
     * Add a token to the blacklist.
     * The token is kept until its own expiration so the scheduled cleanup can
     * remove it afterwards.
     */
    fun blacklist(token: String) {
        val expiry = jwtUtil.extractExpiration(token)
        blacklist[token] = expiry.time
    }

    /** Returns true when the token has been explicitly invalidated. */
    fun isBlacklisted(token: String): Boolean = blacklist.containsKey(token)

    /** Runs every hour and removes entries whose JWT has already expired. */
    @Scheduled(fixedRate = 3_600_000)
    fun purgeExpiredTokens() {
        val now = System.currentTimeMillis()
        blacklist.entries.removeIf { it.value <= now }
    }
}
