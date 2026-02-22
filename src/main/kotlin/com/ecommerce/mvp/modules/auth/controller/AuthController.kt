package com.ecommerce.mvp.modules.auth.controller

import com.ecommerce.mvp.security.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
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
}

// Idiomatic Kotlin Data Class (includes Getters/Setters/Constructor)
data class AuthRequest(
    val username: String,
    val password: String
)
