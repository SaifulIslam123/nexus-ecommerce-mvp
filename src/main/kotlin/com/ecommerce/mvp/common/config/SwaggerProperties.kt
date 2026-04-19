package com.ecommerce.mvp.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "swagger")
data class SwaggerProperties(
    val publicPaths: List<String> = emptyList()
)
