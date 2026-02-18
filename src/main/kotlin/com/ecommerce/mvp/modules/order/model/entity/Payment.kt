package com.ecommerce.mvp.modules.order.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*
import java.math.BigDecimal


@Entity
@Table(name = "payments")
class Payment : BaseEntityAudit() {

    @Column(nullable = false)
    var method: String? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(unique = true)
    var transactionId: String? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    var order: Order? = null

}

