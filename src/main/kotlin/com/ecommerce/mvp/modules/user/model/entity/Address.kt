package com.ecommerce.mvp.modules.user.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*


@Entity
@Table(name = "addresses")
class Address : BaseEntityAudit() {

    @Column(nullable = false)
    var street: String? = null

    @Column(nullable = false)
    var city: String? = null

    @Column(nullable = false)
    var zip: String? = null

    @Column(nullable = false)
    var country: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

}
