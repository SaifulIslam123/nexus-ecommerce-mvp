package com.ecommerce.mvp.modules.user.controller

import com.ecommerce.mvp.modules.user.model.dto.AddressDto
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.model.dto.UserProfileUpdateDto
import com.ecommerce.mvp.modules.user.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getCurrentUserProfile(): UserDto {
        val profile = userService.getCurrentUserProfile()
        return profile
    }

    @PatchMapping("/me")
    fun updateCurrentUserProfile(@Valid @RequestBody updateDto: UserProfileUpdateDto): UserDto {
        val updatedProfile = userService.updateCurrentUserProfile(updateDto)
        return updatedProfile
    }

    @PostMapping("/me/addresses")
    fun addAddress(@Valid @RequestBody addressRequestDto: AddressDto): AddressDto {
        val savedAddress = userService.addAddress(addressRequestDto)
        return savedAddress
    }

}




