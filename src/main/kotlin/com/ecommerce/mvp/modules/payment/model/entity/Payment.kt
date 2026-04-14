package com.ecommerce.mvp.modules.payment.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.order.model.entity.Order
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "payments")
data class Payment(
    @Column(nullable = false)
    var method: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(unique = true)
    var transactionId: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    var order: Order? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.TO_PAY

) : BaseEntityAudit()


// Payment.kt
enum class PaymentStatus {
    TO_PAY,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED   // ← add this
}
