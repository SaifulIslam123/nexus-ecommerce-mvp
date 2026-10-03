package com.ecommerce.mvp.modules.auth.controller

import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.auth.model.AuthRequest
import com.ecommerce.mvp.modules.auth.model.ForgotPasswordRequest
import com.ecommerce.mvp.modules.auth.model.LoginResponseDto
import com.ecommerce.mvp.modules.auth.model.LogoutRequest
import com.ecommerce.mvp.modules.auth.model.RefreshRequest
import com.ecommerce.mvp.modules.auth.model.ResetPasswordRequest
import com.ecommerce.mvp.modules.auth.service.AuthService
import com.ecommerce.mvp.modules.auth.service.PasswordResetService
import com.ecommerce.mvp.modules.auth.service.RefreshTokenService
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.service.UserService
import com.ecommerce.mvp.security.JwtUtil
import com.ecommerce.mvp.security.TokenBlacklistService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as ApiResponseAnnotation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*


@Tag(name = "Authentication", description = "Authentication endpoints including login, register, forgot password, and reset password")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService,
    private val refreshTokenService: RefreshTokenService,
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService
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

    @Operation(
        summary = "Request password reset link",
        description = "Send a password reset link to the user's registered email. Returns 202 regardless of whether the email exists (prevents email enumeration)."
    )
    @ApiResponses(
        value = [
            ApiResponseAnnotation(
                responseCode = "202",
                description = "Password reset link sent (or email not found - response is identical)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            ApiResponseAnnotation(
                responseCode = "400",
                description = "Invalid email format or malformed request",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            ApiResponseAnnotation(
                responseCode = "429",
                description = "Too many requests - rate limit exceeded (3 per email per hour, 10 per IP per hour)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody request: ForgotPasswordRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val clientIp = resolveClientIp(httpServletRequest)
        return passwordResetService.forgotPassword(request.email.orEmpty(), clientIp)
    }

    @Operation(
        summary = "Reset user password",
        description = "Set a new password using a valid reset token. Returns generic error message for any token issues (unknown, expired, or already used) to prevent token enumeration."
    )
    @ApiResponses(
        value = [
            ApiResponseAnnotation(
                responseCode = "200",
                description = "Password has been successfully reset",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            ApiResponseAnnotation(
                responseCode = "400",
                description = "Invalid or expired token, or password validation failed",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            ApiResponseAnnotation(
                responseCode = "429",
                description = "Too many requests - rate limit exceeded (10 per IP per hour)",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<Unit>> {
        return passwordResetService.resetPassword(request.token, request.newPassword)
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

    private fun resolveClientIp(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        if (!forwardedFor.isNullOrBlank()) {
            return forwardedFor.split(",").firstOrNull()?.trim().orEmpty()
        }

        val realIp = request.getHeader("X-Real-IP")
        if (!realIp.isNullOrBlank()) {
            return realIp.trim()
        }

        return request.remoteAddr ?: "unknown"
    }
}
