package com.ecommerce.mvp.modules.user.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "addresses")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
class Address : BaseEntityAudit() {

    @Column(nullable = false)
    var street: String? = null

    @Column(nullable = false)
    var city: String? = null

    @Column(nullable = false)
    var zip: String? = null

    @Column(nullable = false)
    var country: String? = null

    //Used for soft delete, null means active, non-null means deleted
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

}
