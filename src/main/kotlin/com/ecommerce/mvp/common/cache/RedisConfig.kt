package com.ecommerce.mvp.common.cache

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis Cache configuration:
 *
 *  - JSON serialization (human-readable, forward-compatible)
 *  - Polymorphic type info embedded in JSON for correct deserialization
 *  - Per-cache TTL tuned by data volatility
 *  - Null-value caching disabled (avoids storing "not found" as cached entries)
 *  - Key prefix: <cacheName>::<key>  (Spring default)
 */
/** Redis cache manager is only wired in the PROD environment. In DEV, [spring.cache.type=none] is used instead. */
@Configuration
@EnableCaching
@Profile("prod")
class RedisConfig {

    /**
     * Dedicated [ObjectMapper] for Redis serialization.
     *
     * Uses [BasicPolymorphicTypeValidator] with [ObjectMapper.DefaultTyping.NON_FINAL]
     * so Spring's internal types (e.g. [org.springframework.data.domain.PageImpl])
     * are stored with their concrete class name and deserialized back correctly.
     */
    @Bean(name = ["redisObjectMapper"])
    fun redisObjectMapper(): ObjectMapper {
        val ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Any::class.java)
            .build()

        return ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
    }

    /** Shared serializer used for all cache values. */
    private fun valueSerializer(objectMapper: ObjectMapper) =
        GenericJackson2JsonRedisSerializer(objectMapper)

    /**
     * Builds a base [RedisCacheConfiguration] with:
     *  - String key serializer
     *  - JSON value serializer
     *  - Null values NOT cached
     */
    private fun baseCacheConfig(ttl: Duration, objectMapper: ObjectMapper): RedisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer(objectMapper))
            )
            .disableCachingNullValues()

    @Bean
    fun redisCacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        redisObjectMapper: ObjectMapper
    ): RedisCacheManager {

        val defaultTtl = baseCacheConfig(Duration.ofMinutes(10), redisObjectMapper)

        val perCacheConfigs = mapOf(
            // Products — moderate volatility
            CacheNames.PRODUCTS              to baseCacheConfig(Duration.ofMinutes(10), redisObjectMapper),
            CacheNames.PRODUCTS_SEARCH       to baseCacheConfig(Duration.ofMinutes(5),  redisObjectMapper),
            CacheNames.PRODUCTS_RECOMMENDATIONS to baseCacheConfig(Duration.ofMinutes(10), redisObjectMapper),

            // Categories — low volatility (admin rarely changes these)
            CacheNames.CATEGORIES            to baseCacheConfig(Duration.ofMinutes(30), redisObjectMapper),
            CacheNames.CATEGORIES_SINGLE     to baseCacheConfig(Duration.ofMinutes(30), redisObjectMapper),

            // Tags — low volatility
            CacheNames.TAGS                  to baseCacheConfig(Duration.ofMinutes(30), redisObjectMapper),
            CacheNames.TAGS_SINGLE           to baseCacheConfig(Duration.ofMinutes(30), redisObjectMapper),
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultTtl)
            .withInitialCacheConfigurations(perCacheConfigs)
            // Allows the cache manager to create new caches at runtime with the default config
            .transactionAware()
            .build()
    }
}

