package com.ecommerce.mvp.modules.user.model.dto

import com.ecommerce.mvp.modules.user.model.entity.User
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

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
    var password: String? = null,
    @field:NotNull(message = "Required user roles")
    val userRoles: List<String>? = null,
    @field:NotNull(message = "Address is required")
    var address: AddressDto? = null
)

data class AddressDto(
    val street: String?,
    val city: String?,
    val zip: String?,
    val country: String?
)

data class UserProfileUpdateDto(
    @field:jakarta.validation.constraints.Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    @field:jakarta.validation.constraints.Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    val phone: String? = null,
    val address: AddressDto?,
)

fun UserDto.toEntity(): User {

    return User().apply {
        name = this@toEntity.name
        email = this@toEntity.email
        phone = this@toEntity.phone
        password = this@toEntity.password
        address = this@toEntity.address
        // Roles are usually handled separately or looked up from DB, not created from DTO directly in simple mapper
    }

}
