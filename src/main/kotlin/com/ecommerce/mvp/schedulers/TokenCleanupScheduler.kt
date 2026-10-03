package com.ecommerce.mvp.schedulers

import com.ecommerce.mvp.modules.auth.repository.PasswordResetTokenRepository
import com.ecommerce.mvp.modules.auth.repository.RefreshTokenRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TokenCleanupScheduler(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository
) {

    private val logger = LoggerFactory.getLogger(TokenCleanupScheduler::class.java)

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    fun purgeExpiredAndRevokedTokens() {
        val now = Instant.now()
        
        // Clean expired and revoked refresh tokens
        refreshTokenRepository.deleteExpiredAndRevoked(now)
        logger.info("Purged expired and revoked refresh tokens")
        
        // Clean expired and used password reset tokens
        passwordResetTokenRepository.deleteExpiredAndUsedTokens(now)
        logger.info("Purged expired and used password reset tokens")
    }
}
