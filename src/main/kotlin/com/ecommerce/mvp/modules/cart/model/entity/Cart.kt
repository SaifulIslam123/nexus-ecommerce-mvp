package com.ecommerce.mvp.modules.cart.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import com.practice.ecommerce.ecommerce.modules.user.model.entity.User
import jakarta.persistence.*


@Entity
@Table(name = "carts")
class Cart : BaseEntityAudit() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var user: User? = null

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var cartItems: MutableSet<CartItem> = mutableSetOf()

}

