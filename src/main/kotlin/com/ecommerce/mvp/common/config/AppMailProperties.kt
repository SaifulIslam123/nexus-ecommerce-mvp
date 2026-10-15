package com.ecommerce.mvp.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.mail")
data class AppMailProperties(
    val from: String = "noreply@nexus.ecommerce"
)

