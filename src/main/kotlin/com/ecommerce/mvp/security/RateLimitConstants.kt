package com.ecommerce.mvp.security

object RateLimitConstants {

    // Authenticated users (identified by userId from JWT)
    const val AUTH_LIMIT = 100L
    const val AUTH_WINDOW_SECONDS = 60L

    // Public endpoints (identified by IP)
    const val PUBLIC_LIMIT = 30L
    const val PUBLIC_WINDOW_SECONDS = 60L

    // Sensitive endpoints: login, register (identified by IP)
    const val SENSITIVE_LIMIT = 5L
    const val SENSITIVE_WINDOW_SECONDS = 60L

    // Redis key prefix — avoids clashing with your other Redis data
    const val KEY_PREFIX = "rate_limit"

     const val FORGOT_PASSWORD_EMAIL_LIMIT = 3L
     const val FORGOT_PASSWORD_IP_LIMIT = 10L
     const val FORGOT_PASSWORD_WINDOW_SECONDS = 3600L
}