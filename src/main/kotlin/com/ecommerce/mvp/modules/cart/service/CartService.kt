package com.ecommerce.mvp.modules.cart.service

import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.cart.model.dto.CartResponseDto
import com.ecommerce.mvp.modules.cart.model.dto.toResponseDto
import com.ecommerce.mvp.modules.cart.repository.CartRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository
) {

    /**
     * Returns the cart (with calculated totals) for the currently
     * authenticated user.  Throws [ResourceNotFoundException] if the
     * user has no cart yet.
     */
    @Transactional(readOnly = true)
    fun getCart(): CartResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cart = cartRepository.findByUserEmail(email)
        return cart?.toResponseDto()
            ?: throw ResourceNotFoundException("Cart not found for user: $email")

    }
}

