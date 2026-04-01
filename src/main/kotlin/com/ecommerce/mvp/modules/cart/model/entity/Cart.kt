package com.ecommerce.mvp.modules.cart.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.*

@Entity
@Table(name = "carts")
data class Cart(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var user: User,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var cartItems: MutableSet<CartItem> = mutableSetOf()

) : BaseEntityAudit()
