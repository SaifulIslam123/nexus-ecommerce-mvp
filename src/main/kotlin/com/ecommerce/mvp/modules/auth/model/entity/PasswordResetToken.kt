package com.ecommerce.mvp.modules.auth.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken : BaseEntityAudit() {

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    var tokenHash: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant = Instant.now()

    @Column(name = "used_at")
    var usedAt: Instant? = null
}

