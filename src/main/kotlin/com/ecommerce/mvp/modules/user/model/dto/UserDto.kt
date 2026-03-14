package com.ecommerce.mvp.modules.user.model.dto

import com.ecommerce.mvp.common.AppConstant.MAX_Address
import com.ecommerce.mvp.modules.user.model.entity.Address
import com.ecommerce.mvp.modules.user.model.entity.User
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
    @field:jakarta.validation.constraints.Size(max = MAX_Address, message = "Maximum $MAX_Address addresses allowed")
    var address: List<AddressDto>? = null
)

fun UserDto.toEntity(): User {

    return User().apply {
        name = this@toEntity.name
        email = this@toEntity.email
        phone = this@toEntity.phone
        password = this@toEntity.password

        // Roles and Address are usually handled separately or looked up from DB, not created from DTO directly in simple mapper
    }
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = this.id,
        name = this.name,
        email = this.email,
        phone = this.phone,
        userRoles = this.userRoles.map { it.name.toString() },
        address = this.addresses.map { address ->
            AddressDto(
                id = address.id,
                street = address.street,
                city = address.city,
                zip = address.zip,
                country = address.country
            )
        }
    )
}


data class AddressDto(
    var id: Long? = null,
    @field:NotBlank(message = "Street is required")
    @field:jakarta.validation.constraints.Size(max = 255, message = "Street must not exceed 255 characters")
    val street: String?,

    @field:NotBlank(message = "City is required")
    @field:jakarta.validation.constraints.Size(max = 100, message = "City must not exceed 100 characters")
    val city: String?,

    @field:NotBlank(message = "Zip code is required")
    @field:jakarta.validation.constraints.Pattern(regexp = "^[A-Za-z0-9\\-\\s]{3,10}$", message = "Invalid zip code format")
    val zip: String?,

    @field:NotBlank(message = "Country is required")
    @field:jakarta.validation.constraints.Size(max = 100, message = "Country must not exceed 100 characters")
    val country: String?
)

fun Address.toAddressDto(): AddressDto {
    return AddressDto(
        id = this.id,
        street = this.street,
        city = this.city,
        zip = this.zip,
        country = this.country
    )
}

data class UserProfileUpdateDto(
    @field:jakarta.validation.constraints.Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    @field:jakarta.validation.constraints.Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    val phone: String? = null,
    @field:jakarta.validation.constraints.Size(max = MAX_Address, message = "Maximum $MAX_Address addresses allowed")
    var address: List<AddressDto>? = null
)


