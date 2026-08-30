package com.ecommerce.mvp.modules.auth.controller

import com.ecommerce.mvp.modules.auth.model.AuthRequest
import com.ecommerce.mvp.modules.auth.model.LoginResponseDto
import com.ecommerce.mvp.modules.auth.model.LogoutRequest
import com.ecommerce.mvp.modules.auth.model.RefreshRequest
import com.ecommerce.mvp.modules.auth.service.AuthService
import com.ecommerce.mvp.modules.auth.service.RefreshTokenService
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.service.UserService
import com.ecommerce.mvp.security.JwtUtil
import com.ecommerce.mvp.security.TokenBlacklistService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService,
    private val refreshTokenService: RefreshTokenService,
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): LoginResponseDto {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.userEmail, request.password)
        )

        return authService.login(request)

    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserDto): UserDto {
        return userService.registerUser(request.apply {
            password = passwordEncoder.encode(password)
        })
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody body: RefreshRequest): LoginResponseDto {
        return authService.refresh(body)
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, @RequestBody logoutRequest: LogoutRequest): String {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authHeader.substring(7))
        }
        logoutRequest.refreshToken?.let { refreshTokenService.revokeToken(it) }
        return "Logged out successfully"
    }
}
