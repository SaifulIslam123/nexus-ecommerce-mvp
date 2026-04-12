package com.ecommerce.mvp.modules.order.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.payment.model.entity.Payment
import com.ecommerce.mvp.modules.user.model.entity.Address
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant


@Entity
@Table(name = "Orders")
data class Order(
    @field:Column(nullable = false)
    var orderDate: Instant = Instant.now(),

    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var orderItems: MutableSet<OrderItem> = mutableSetOf(),

    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var payment: Payment? = null,

    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var shipment: Shipment? = null,


) : BaseEntityAudit()

enum class OrderStatus {
    /** Order created but payment is not yet verified.  */
    TO_PAY,

    /** Payment successfully received and order confirmed.  */
    CONFIRMED,

    /** Order is being picked, packed, or manufactured.  */
    PROCESSING,

    /** Package has been handed over to the shipping carrier.  */
    SHIPPED,

    /** Package is with the local courier for final delivery.  */
    OUT_FOR_DELIVERY,

    /** Package has been successfully deliver by the courier to customer.  */
    DELIVERED,

    /** Package has been successfully received by the customer.  */
    RECEIVED,

    /** Order was cancelled by the customer or the system.  */
    CANCELLED,

    /** Payment failed or was declined by the gateway.  */
    FAILED,

    /** Items were returned by the customer after delivery.  */
    RETURNED,

    /** Funds have been returned to the customer.  */
    REFUNDED
}
