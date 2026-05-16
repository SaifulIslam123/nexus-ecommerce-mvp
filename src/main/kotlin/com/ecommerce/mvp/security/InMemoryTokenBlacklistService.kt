package com.ecommerce.mvp.security

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * DEV-only in-memory token blacklist.
 *
 * Uses a [ConcurrentHashMap] keyed by the raw JWT string.
 * A scheduled task purges entries whose tokens have already expired, keeping
 * memory usage bounded without requiring a Redis instance locally.
 *
 * NOT suitable for multi-instance deployments — each node would maintain its
 * own map. Use [RedisTokenBlacklistService] in production instead.
 */
@Service
@Profile("dev")
class InMemoryTokenBlacklistService(
    private val jwtUtil: JwtUtil
) : TokenBlacklistService {

    // token -> expiry time in epoch-millis
    private val store = ConcurrentHashMap<String, Long>()

    override fun blacklist(token: String) {
        val expiry = jwtUtil.extractExpiration(token)
        store[token] = expiry.time
    }

    override fun isBlacklisted(token: String): Boolean = store.containsKey(token)

    /** Runs every hour and removes entries whose JWT has already expired. */
    @Scheduled(fixedRate = 3_600_000)
    fun purgeExpiredTokens() {
        val now = System.currentTimeMillis()
        store.entries.removeIf { it.value <= now }
    }
}

