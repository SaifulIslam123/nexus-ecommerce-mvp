package com.ecommerce.mvp.modules.auth.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


data class AuthRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val userEmail: String?,
    @field:NotBlank(message = "Password is required")
    val password: String?
)
