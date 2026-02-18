package com.ecommerce.mvp.modules.user.service

import com.practice.ecommerce.ecommerce.modules.user.model.dto.UserDto
import com.practice.ecommerce.ecommerce.modules.user.model.entity.User

object UserMapper {
    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            userRoles = user.userRoles.mapNotNull { it.name?.name }.toSet()
        )
    }

    fun toEntity(userDto: UserDto): User {
        return User().apply {
            name = userDto.name
            email = userDto.email
            phone = userDto.phone
            password = userDto.password
            // Roles are usually handled separately or looked up from DB, not created from DTO directly in simple mapper
        }
    }
}

