package com.ecommerce.mvp.modules.auth.repository

import com.ecommerce.mvp.modules.auth.model.entity.PasswordResetToken
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {

    fun findByTokenHashAndUsedAtNull(tokenHash: String): Optional<PasswordResetToken>

    fun findByUserIdAndUsedAtNull(userId: Long): List<PasswordResetToken>

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken p SET p.usedAt = :now WHERE p.user.id = :userId AND p.usedAt IS NULL")
    fun markAllUserTokensAsUsed(userId: Long, now: Instant)

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now OR p.usedAt IS NOT NULL")
    fun deleteExpiredAndUsedTokens(now: Instant)

}

