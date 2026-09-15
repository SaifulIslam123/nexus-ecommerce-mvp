package com.ecommerce.mvp.security

import com.ecommerce.mvp.common.cache.RedisCacheErrorHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    private val logger: Logger = LoggerFactory.getLogger(RedisTokenBlacklistService::class.java)

    override fun blacklist(token: String) {
        try {
            val expiry = jwtUtil.extractExpiration(token).toInstant()
            val ttl = Duration.between(Instant.now(), expiry)
            if (!ttl.isNegative) {
                redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", ttl)
            }
        } catch (ex: Exception) {
            logger.error("Redis error on blacklist", ex)
            // Fail-open: log and continue, or fail-closed: throw
        }
    }

    override fun isBlacklisted(token: String): Boolean {
        return try {
            redisTemplate.hasKey(KEY_PREFIX + token) == true
        } catch (ex: Exception) {
            logger.error("Redis error on blacklist check", ex)
            false  // Fail-open: assume not blacklisted
        }
    }
}

