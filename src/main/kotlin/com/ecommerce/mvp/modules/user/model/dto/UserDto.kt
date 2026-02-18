package com.ecommerce.mvp.modules.user.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserDto(
    val id: Long? = null,
    @field:NotBlank(message = "Name is required")
    val name: String?,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String?,
    val phone: String?,
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    val password: String? = null,
    val userRoles: Set<String>? = null
)
data class UserEmailDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
)

