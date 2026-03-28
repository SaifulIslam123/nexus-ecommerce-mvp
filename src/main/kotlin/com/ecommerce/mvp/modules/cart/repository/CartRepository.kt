package com.ecommerce.mvp.modules.cart.repository

import com.ecommerce.mvp.modules.cart.model.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CartRepository : JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c JOIN FETCH c.user b WHERE b.email = :email")
    fun findByUserEmail(email: String): Cart?
}

