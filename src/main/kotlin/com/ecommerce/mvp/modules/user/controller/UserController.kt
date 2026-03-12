package com.ecommerce.mvp.modules.user.controller

import com.ecommerce.mvp.ResponseMessage
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.model.entity.User
import com.ecommerce.mvp.modules.user.model.dto.UserProfileUpdateDto
import com.ecommerce.mvp.modules.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {


    @PostMapping("/create")
    fun createUser(@RequestBody user: User): ResponseEntity<Any> {
        try {
            val savedUser = userService.createUser(user)
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                    ResponseMessage(
                        isSuccessful = false,
                        code = HttpStatus.CREATED.value(),
                        "User created successfully"
                    )
                )
        } catch (ex: Exception) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ResponseMessage(
                    isSuccessful = false,
                    code = HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    ex.message ?: "An error occurred"
                )
            )
        }

    }

    @GetMapping("/me")
    fun getCurrentUserProfile(): ResponseEntity<UserDto> {
        val profile = userService.getCurrentUserProfile()
        return ResponseEntity.ok(profile)
    }

    @PatchMapping("/me")
    fun updateCurrentUserProfile(@Valid @RequestBody updateDto: UserProfileUpdateDto): ResponseEntity<UserDto> {
        val updatedProfile = userService.updateCurrentUserProfile(updateDto)
        return ResponseEntity.ok(updatedProfile)
    }

}




