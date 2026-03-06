package com.ecommerce.mvp.modules.user.controller

import com.ecommerce.mvp.ResponseMessage
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.model.entity.User
import com.ecommerce.mvp.modules.user.model.dto.UserProfileUpdateDto
import com.ecommerce.mvp.modules.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    /*@GetMapping
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val users = userService.findAll()
        val userDtos = users.map { UserMapper.toDto(it) }
        return ResponseEntity.ok(userDtos)
    }*/

   /* @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        println("Fetching user with ID: $id")
        val user = userService.findById(id)
        return if (user != null) {
            ResponseEntity.ok(user*//*UserMapper.toDto(user)*//*)
        } else {
            ResponseEntity.notFound().build()
        }

    }*/

    /*@PostMapping("/create-without-password")
    fun createUserWithPassword(@Valid @RequestBody userDto: UserDto): ResponseEntity<Any> {
        if (userDto.password.isNullOrBlank() || userDto.password.length < 6) {
           *//* return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage(isSuccessful = false,
                code = HttpStatus.BAD_REQUEST.value(),
                message = "Password must be at least 6 characters long"))*//*
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).
            body(
                ResponseMessage(
                    isSuccessful = true,
                    code = HttpStatus.PRECONDITION_FAILED.value(),
                    "Password must be at least 6 characters long"
                )
            )
        }
        val user = UserMapper.toEntity(userDto)
        // In a real app, you'd encode the password here or in the service
        val savedUser = userService.registerUser(user, userDto.userRoles)
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(user))

    }*/

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


    /*@PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody userDto: UserDto): ResponseEntity<UserDto> {
        val existingUser = userService.findById(id) ?: return ResponseEntity.notFound().build()

        // Update fields
        existingUser.name = userDto.name ?: existingUser.name
        existingUser.email = userDto.email ?: existingUser.email
        existingUser.phone = userDto.phone ?: existingUser.phone
        if (userDto.password != null) {
            existingUser.password = userDto.password
        }

        val updatedUser = userService.save(existingUser)
        return ResponseEntity.ok(UserMapper.toDto(updatedUser))
    }*/

    /* @DeleteMapping("/{id}")
     fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
         if (userService.findById(id) == null) {
             return ResponseEntity.notFound().build()
         }
         userService.deleteById(id)
         return ResponseEntity.noContent().build()
     }*/


    /*@GetMapping("/emailuser")
    fun getUserOrderByEmail(@Valid @RequestBody  email: UserEmailDto): ResponseEntity<User> {
        return userService.findUserOrderByEmail(email.email)
            .map({ user -> ResponseEntity.ok(user) }) // Automatically sets status to 200 OK
            .orElse(ResponseEntity.notFound().build())
    }*/

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUserProfile(): ResponseEntity<UserDto> {
        val profile = userService.getCurrentUserProfile()
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    fun updateCurrentUserProfile(@RequestBody updateDto: UserProfileUpdateDto): ResponseEntity<UserDto> {
        val updatedProfile = userService.updateCurrentUserProfile(updateDto)
        return ResponseEntity.ok(updatedProfile)
    }
}




