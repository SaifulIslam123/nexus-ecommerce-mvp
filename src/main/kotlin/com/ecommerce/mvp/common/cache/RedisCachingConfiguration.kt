package com.ecommerce.mvp.common.cache

import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Configuration


@Configuration
class RedisCachingConfiguration : CachingConfigurer {

    override fun errorHandler(): CacheErrorHandler? {
        return RedisCacheErrorHandler()
    }
}