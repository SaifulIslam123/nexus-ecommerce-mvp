package com.ecommerce.mvp.security

import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

/**
 * PROD Redis-backed token blacklist.
 *
 * Each invalidated JWT is stored in Redis as:
 *   Key   : "token:blacklist:<jwt>"
 *   Value : "1"  (a cheap sentinel — the key's existence is all that matters)
 *   TTL   : remaining lifetime of the token (seconds)
 *
 * Redis automatically evicts the key when the token would have expired anyway,
 * so no scheduled cleanup job is needed and memory usage stays perfectly bounded.
 *
 * Because Redis is a shared store, this works correctly across multiple
 * application instances (horizontal scaling / Kubernetes replicas).
 */
@Service
@Profile("prod")
class RedisTokenBlacklistService(
    private val jwtUtil: JwtUtil,
    private val redisTemplate: StringRedisTemplate
) : TokenBlacklistService {

    companion object {
        private const val KEY_PREFIX = "token:blacklist:"
    }

    override fun blacklist(token: String) {
        val expiry   = jwtUtil.extractExpiration(token).toInstant()
        val ttl      = Duration.between(Instant.now(), expiry)

        // Only store the key if the token hasn't already expired
        if (!ttl.isNegative && !ttl.isZero) {
            redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", ttl)
        }
    }

    override fun isBlacklisted(token: String): Boolean =
        redisTemplate.hasKey(KEY_PREFIX + token) == true
}

