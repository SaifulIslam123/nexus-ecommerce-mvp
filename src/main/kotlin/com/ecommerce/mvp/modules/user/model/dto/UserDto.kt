package com.ecommerce.mvp.modules.user.model.dto

import com.ecommerce.mvp.modules.category.dto.CategoryDto
import com.ecommerce.mvp.modules.category.entity.Category
import com.ecommerce.mvp.modules.user.model.entity.User
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserDto(
    var id: Long? = null,
    @field:NotBlank(message = "Name is required")
    val name: String?,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String?,
    @field:NotBlank(message = "Phone number is required")
    val phone: String?,
    @field:NotBlank(message = "Password is required")
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @JsonIgnore
    var password: String? = null,
    val userRoles: List<String>? = null
)
data class UserEmailDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
)

fun UserDto.toEntity(): User {

    return User().apply {
        name = this@toEntity.name
        email = this@toEntity.email
        phone = this@toEntity.phone
        password = this@toEntity.password
        // Roles are usually handled separately or looked up from DB, not created from DTO directly in simple mapper
    }

}

