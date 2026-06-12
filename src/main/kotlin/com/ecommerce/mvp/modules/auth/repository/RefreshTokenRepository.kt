package com.ecommerce.mvp.modules.auth.repository

import com.ecommerce.mvp.modules.auth.model.entity.RefreshToken
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, String> {

    fun findByIdAndRevokedFalse(id: String): Optional<RefreshToken>

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId")
    fun revokeAllByUserId(userId: Long)

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    fun deleteExpiredAndRevoked(now: Instant)
}
