package com.ecommerce.mvp.modules.auth.service

import com.ecommerce.mvp.common.email.EmailSender
import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.InvalidPasswordResetTokenException
import com.ecommerce.mvp.common.exception.RateLimitException
import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.auth.model.entity.PasswordResetToken
import com.ecommerce.mvp.modules.auth.repository.PasswordResetTokenRepository
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.security.RateLimitService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

@Service
class PasswordResetService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val emailSender: EmailSender,
    private val rateLimitService: RateLimitService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${app.frontend-url:http://localhost:3000}") private val frontendUrl: String
) {

    companion object {
        private const val RESET_TOKEN_TTL_SECONDS = 1800L
    }

    @Transactional
    fun forgotPassword(email: String, clientIp: String): ResponseEntity<ApiResponse<Unit>> {
        val normalizedEmail = email.trim()

        if (normalizedEmail.isBlank() || !isValidEmail(normalizedEmail)) {
            throw BusinessValidationException("Invalid email format")
        }

        if (!rateLimitService.isForgotPasswordAllowed(normalizedEmail, clientIp)) {
            throw RateLimitException("Rate limit exceeded. Please try again later.")
        }

        val user = userRepository.findByUserEmail(normalizedEmail)
            ?: userRepository.findAll().firstOrNull { it.email.equals(normalizedEmail, ignoreCase = true) }

        if (user != null) {
            passwordResetTokenRepository.markAllUserTokensAsUsed(user.id ?: 0L, Instant.now())

            val rawToken = generateRawToken()
            val resetToken = PasswordResetToken().apply {
                tokenHash = hashToken(rawToken)
                this.user = user
                expiresAt = Instant.now().plusSeconds(RESET_TOKEN_TTL_SECONDS)
                usedAt = null
            }

            passwordResetTokenRepository.save(resetToken)
            emailSender.sendPasswordResetEmail(user.email ?: normalizedEmail, rawToken, frontendUrl)
        }

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(
                ApiResponse(
                    success = true,
                    message = "If the account exists, a reset link has been sent."
                )
            )
    }

    @Transactional
    fun resetPassword(token: String?, newPassword: String?): ResponseEntity<ApiResponse<Unit>> {
        val rawToken = token?.trim().orEmpty()
        val candidatePassword = newPassword?.trim().orEmpty()

        if (rawToken.isBlank() || candidatePassword.isBlank()) {
            throw BusinessValidationException("Token and password are required")
        }

        val tokenHash = hashToken(rawToken)
        val resetToken = passwordResetTokenRepository.findByTokenHashAndUsedAtNull(tokenHash)
            .orElseThrow { InvalidPasswordResetTokenException("Invalid or expired token") }

        if (resetToken.expiresAt.isBefore(Instant.now())) {
            throw InvalidPasswordResetTokenException("Invalid or expired token")
        }

        val user = resetToken.user
            ?: throw InvalidPasswordResetTokenException("Invalid or expired token")

        if (passwordEncoder.matches(candidatePassword, user.password ?: "")) {
            throw BusinessValidationException("New password must be different from the current password")
        }

        user.password = passwordEncoder.encode(candidatePassword)
        resetToken.usedAt = Instant.now()

        passwordResetTokenRepository.save(resetToken)
        refreshTokenService.revokeAllForUser(user.id ?: 0L)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Password has been reset."
            )
        )
    }

    private fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(rawToken.toByteArray(StandardCharsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateRawToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.indexOf('@') > 0 && email.substringAfterLast('@').contains('.')
    }
}

