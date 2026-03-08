package com.ecommerce.mvp.modules.auth.model

// Idiomatic Kotlin Data Class (includes Getters/Setters/Constructor)
data class AuthRequest(
    val username: String,
    val password: String
)
