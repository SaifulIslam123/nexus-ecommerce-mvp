package com.ecommerce.mvp.modules.order.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*


@Entity
@Table(name = "Orders")
class Order : BaseEntityAudit() {

    @field:Temporal(TemporalType.TIMESTAMP)
    @field:Column(nullable = false)
    var orderDate: Date = Date()

    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var orderItems: MutableSet<OrderItem> = mutableSetOf()

    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var payment: Payment? = null

    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var shipment: Shipment? = null

}

enum class OrderStatus {
    PENDING,
    PROCESSING,
    PAID,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED
}
