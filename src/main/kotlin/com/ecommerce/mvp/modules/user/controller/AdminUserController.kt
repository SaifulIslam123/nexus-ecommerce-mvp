package com.ecommerce.mvp.modules.user.controller

import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val userService: UserService
) {

    /**
     * GET /api/admin/users
     *
     * Returns a list of all registered users in the system.
     * Includes each user's profile, roles, and non-deleted addresses.
     */
    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return userService.getAllUsers()
    }

    /**
     * GET /api/admin/users/{id}
     *
     * Returns the full profile of a single user by their ID.
     *
     * Responds with 404 if the user does not exist.
     */
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserDto {
        return userService.getUserById(id)
    }

    /**
     * DELETE /api/admin/users/{id}
     *
     * Permanently removes a user and all their associated data
     * (orders, cart, addresses — all cascaded by the entity mappings).
     *
     * Use with caution. Responds with 204 No Content on success.
     * Responds with 404 if the user does not exist.
     */
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        userService.deleteUser(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}

