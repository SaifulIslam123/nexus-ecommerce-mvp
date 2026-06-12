package com.ecommerce.mvp.modules.auth.service

import com.ecommerce.mvp.common.exception.InvalidRefreshTokenException
import com.ecommerce.mvp.modules.auth.model.entity.RefreshToken
import com.ecommerce.mvp.modules.auth.repository.RefreshTokenRepository
import com.ecommerce.mvp.modules.user.model.entity.User
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.security.CustomUserDetails
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

data class RefreshResult(
    val newRefreshTokenId: String,
    val userEmail: String,
    val roles: List<String>
)

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${app.jwt.refresh-token-expiration-ms}") private val refreshTokenExpirationMs: Long
) {

    @Transactional
    fun createRefreshToken(user: CustomUserDetails): String {
        val token = RefreshToken().apply {
            refreshToken =  UUID.randomUUID().toString()
            this.user = userRepository.getReferenceById(user.id)
            expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs)
        }
        refreshTokenRepository.save(token).also {
            return it.refreshToken
        }
    }

    @Transactional
    fun rotateRefreshToken(tokenId: String): RefreshResult {
        val old = refreshTokenRepository.findByIdAndRevokedFalse(tokenId)
            .orElseThrow { InvalidRefreshTokenException("Invalid or expired refresh token. Need to login again") }

        if (old.expiresAt.isBefore(Instant.now())) {
            old.revoked = true
         //   refreshTokenRepository.save(old)
            throw InvalidRefreshTokenException("Invalid or expired refresh token. Need to login again")
        }

        old.revoked = true
        //refreshTokenRepository.save(old)

        val user = old.user!!
        val roles = user.userRoles.map { it.name!!.name }

        val newToken = RefreshToken().apply {
            this.refreshToken = UUID.randomUUID().toString()
            this.user = user
            expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs)
        }
        val saved = refreshTokenRepository.save(newToken)

        return RefreshResult(
            newRefreshTokenId = saved.refreshToken,
            userEmail = user.email!!,
            roles = roles
        )
    }

    @Transactional
    fun revokeToken(tokenId: String) {
        refreshTokenRepository.findById(tokenId).ifPresent { token ->
            if (!token.revoked) {
                token.revoked = true
                refreshTokenRepository.save(token)
            }
        }
    }

    @Transactional
    fun revokeAllForUser(userId: Long) {
        refreshTokenRepository.revokeAllByUserId(userId)
    }
}
