package com.ecommerce.mvp.modules.cart.repository

import com.ecommerce.mvp.modules.cart.model.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {

    /**
     * Finds a cart item by its ID, but only if it belongs to the cart
     * owned by the given user email.  This prevents users from modifying
     * items that belong to another user's cart.
     */

    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c JOIN c.user u WHERE u.email = :email AND ci.id = :itemId")
    fun findByIdAndUserEmail(itemId: Long, email: String): CartItem?

    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c JOIN c.user u JOIN u.addresses ad WHERE u.email = :email AND ci.id = :itemId AND ad.id = :addressId")
    fun findByIdAndUserEmailAndUserAddressId(itemId: Long, email: String, addressId: Long): CartItem?
}

