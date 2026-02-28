package com.ecommerce.mvp.modules.auth.controller

import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.service.UserService
import com.ecommerce.mvp.security.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<String> {
        // Authenticate the user
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // If authentication successful, generate token
        val token = jwtUtil.generateToken(request.username)
        return ResponseEntity.ok(token)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserDto): ResponseEntity<UserDto> {

        val userDto = userService.registerUser(request.apply {
            password = passwordEncoder.encode(password)
        })
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto)
    }

}

// Idiomatic Kotlin Data Class (includes Getters/Setters/Constructor)
data class AuthRequest(
    val username: String,
    val password: String
)
