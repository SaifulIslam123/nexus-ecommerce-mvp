package com.ecommerce.mvp.schedulers

import com.ecommerce.mvp.modules.auth.repository.RefreshTokenRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TokenCleanupScheduler(private val refreshTokenRepository: RefreshTokenRepository) {

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    fun purgeExpiredAndRevokedTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now())
    }
}
