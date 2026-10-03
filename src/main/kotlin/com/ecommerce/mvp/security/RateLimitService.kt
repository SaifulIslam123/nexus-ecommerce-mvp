package com.ecommerce.mvp.security

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Service

@Service
class RateLimitService(
    private val redisTemplate: StringRedisTemplate
) {

    companion object {
        private const val FORGOT_PASSWORD_EMAIL_LIMIT = 3L
        private const val FORGOT_PASSWORD_IP_LIMIT = 10L
        private const val FORGOT_PASSWORD_WINDOW_SECONDS = 3600L
    }

    /**
     * Lua script — runs atomically in Redis (no race conditions).
     * Logic:
     *   1. Increment the counter for this key
     *   2. If it's a brand new key, set expiry (the time window)
     *   3. Return the current count
     */
    private val rateLimitScript = RedisScript.of(
        """
        local count = redis.call('INCR', KEYS[1])
        if count == 1 then
            redis.call('EXPIRE', KEYS[1], ARGV[1])
        end
        return count
        """.trimIndent(),
        Long::class.java
    )

    /**
     * Returns true if the request is allowed.
     * Returns false if the rate limit is exceeded.
     */
    fun isAllowed(key: String, limit: Long, windowSeconds: Long): Boolean {
        val redisKey = "${RateLimitConstants.KEY_PREFIX}:$key"
        val count = redisTemplate.execute(
            rateLimitScript,
            listOf(redisKey),
            windowSeconds.toString()
        ) ?: 0L

        return count <= limit
    }

    /**
     * Returns how many requests the client has made in the current window.
     * Used to populate X-RateLimit-Remaining header.
     */
    fun getCurrentCount(key: String): Long {
        val redisKey = "${RateLimitConstants.KEY_PREFIX}:$key"
        return redisTemplate.opsForValue().get(redisKey)?.toLong() ?: 0L
    }

    fun isForgotPasswordAllowed(email: String, ip: String): Boolean {
        return isForgotPasswordAllowed(
            email = email,
            ip = ip,
            emailLimit = FORGOT_PASSWORD_EMAIL_LIMIT,
            ipLimit = FORGOT_PASSWORD_IP_LIMIT
        )
    }

    fun isForgotPasswordAllowed(
        email: String,
        ip: String,
        emailLimit: Long,
        ipLimit: Long
    ): Boolean {
        val normalizedEmail = email.trim().lowercase()
        val normalizedIp = ip.trim()

        val emailAllowed = isAllowed(
            key = "forgot-password:email:$normalizedEmail",
            limit = emailLimit,
            windowSeconds = FORGOT_PASSWORD_WINDOW_SECONDS
        )
        val ipAllowed = isAllowed(
            key = "forgot-password:ip:$normalizedIp",
            limit = ipLimit,
            windowSeconds = FORGOT_PASSWORD_WINDOW_SECONDS
        )

        return emailAllowed && ipAllowed
    }
}