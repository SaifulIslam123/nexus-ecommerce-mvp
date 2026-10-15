package com.ecommerce.mvp.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class AppJwtProperties(
    val secret: String,
    val accessTokenExpirationMs: Long = 900_000,
    val refreshTokenExpirationMs: Long = 604_800_000
)

