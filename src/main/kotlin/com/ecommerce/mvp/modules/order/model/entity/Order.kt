package com.ecommerce.mvp.modules.order.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.payment.model.entity.Payment
import com.ecommerce.mvp.modules.user.model.entity.Address
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*


@Entity
@Table(name = "Orders")
data class Order(
    @field:Temporal(TemporalType.TIMESTAMP)
    @field:Column(nullable = false)
    var orderDate: Date = Date(),

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_address_id", nullable = false)
    var shipmentAddress: Address

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

    /** Package has been successfully received by the customer.  */
    DELIVERED,

    /** Order was cancelled by the customer or the system.  */
    CANCELLED,

    /** Payment failed or was declined by the gateway.  */
    FAILED,

    /** Items were returned by the customer after delivery.  */
    RETURNED,

    /** Funds have been returned to the customer.  */
    REFUNDED
}
