package com.ecommerce.mvp.modules.auth.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(
    description = "Request body for reset password endpoint",
    example = """{"token": "eyJhbGciOi...", "newPassword": "NewPassword123!"}"""
)
data class ResetPasswordRequest(
    @field:NotBlank(message = "Token is required")
    @Schema(description = "Password reset token received via email", example = "eyJhbGciOi...")
    val token: String?,
    @field:NotBlank(message = "Password is required")
    @Schema(description = "New password for the user account", example = "NewPassword123!")
    val newPassword: String?
)

