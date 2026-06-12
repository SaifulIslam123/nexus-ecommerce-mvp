package com.ecommerce.mvp.modules.auth.model

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)