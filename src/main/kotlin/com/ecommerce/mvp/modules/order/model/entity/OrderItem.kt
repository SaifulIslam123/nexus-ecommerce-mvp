package com.ecommerce.mvp.modules.order.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import com.practice.ecommerce.ecommerce.modules.product.model.entity.Product
import jakarta.persistence.*
import java.math.BigDecimal


@Entity
@Table(name = "order_items")
class OrderItem : BaseEntityAudit() {

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    var product: Product? = null

}
