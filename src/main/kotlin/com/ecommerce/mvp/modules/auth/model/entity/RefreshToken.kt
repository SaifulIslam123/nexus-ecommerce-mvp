package com.ecommerce.mvp.modules.auth.model.entity

import com.ecommerce.mvp.common.entity.BaseEntity
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshToken : BaseEntity() {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
     var refreshToken: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(nullable = false)
    var expiresAt: Instant = Instant.now()

    @Column(nullable = false)
    var revoked: Boolean = false
}
