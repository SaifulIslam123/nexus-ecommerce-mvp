package com.ecommerce.mvp.common.cache

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler
import java.lang.RuntimeException

class RedisCacheErrorHandler : CacheErrorHandler {
    private val logger: Logger = LoggerFactory.getLogger(RedisCacheErrorHandler::class.java)

    override fun handleCacheGetError(
        exception: RuntimeException,
        cache: Cache,
        key: Any
    ) {
        logger.error("Error occurred while fetching cache entry for key: {}", key, exception)
    }


    override fun handleCachePutError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
        value: Any?
    ) {
        logger.error("Error occurred while putting cache entry for key: {}", key, exception)
    }

    override fun handleCacheEvictError(
        exception: RuntimeException,
        cache: Cache,
        key: Any
    ) {
        logger.error("Error occurred while evicting cache entry for key: {}", key, exception)
    }

    override fun handleCacheClearError(exception: RuntimeException, cache: Cache) {
        logger.error("Error occurred while clearing cache", exception)
    }
}