package com.ecommerce.mvp.modules.cart.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.product.model.entity.Product
import jakarta.persistence.*
import lombok.ToString
import java.math.BigDecimal


/*@Entity
@Table(name = "cart_items")
class CartItem : BaseEntityAudit() {

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    lateinit var cart: Cart

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

}*/

@Entity
@Table(name = "cart_items")
data class CartItem(
    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @ToString.Exclude
    var cart: Cart,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,
) : BaseEntityAudit()/*{
    override fun toString(): String {
        return "CartItem(quantity=$quantity, price=$price, product=$product)"
    }
}*/

