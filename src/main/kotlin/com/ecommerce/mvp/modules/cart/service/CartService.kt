package com.ecommerce.mvp.modules.cart.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.cart.model.dto.CartItemRequestDto
import com.ecommerce.mvp.modules.cart.model.dto.CartResponseDto
import com.ecommerce.mvp.modules.cart.model.dto.toResponseDto
import com.ecommerce.mvp.modules.cart.model.entity.Cart
import com.ecommerce.mvp.modules.cart.model.entity.CartItem
import com.ecommerce.mvp.modules.cart.repository.CartItemRepository
import com.ecommerce.mvp.modules.cart.repository.CartRepository
import com.ecommerce.mvp.modules.product.repository.ProductRepository
import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository
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

    @Transactional
    fun addItemToCart(requestDto: CartItemRequestDto): CartResponseDto {

        val product = productRepository.findById(requestDto.productId )
            .orElseThrow { ResourceNotFoundException("Product not found with id: ${requestDto.productId}") }

        validateQuantityStock(requestDto.quantity, product.stock)

        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")

        val cart = Cart().apply {
            this.user = user
        }

        val cartItem = CartItem().apply {
            this.product = product
            this.quantity = requestDto.quantity
            this.price = product.price
            this.cart = cart
        }

        cart.cartItems.add(cartItem)

        val savedCart = cartRepository.save(cart)

        return savedCart.toResponseDto()

    }

    /**
     * Updates the quantity of an existing cart item identified by [itemId].
     * Only the item's owning user (resolved from the JWT) may perform this.
     * Throws [ResourceNotFoundException] if the item doesn't exist or doesn't
     * belong to the current user.
     * Throws [BusinessValidationException] if the requested quantity exceeds
     * available product stock.
     */
    @Transactional
    fun updateCartItemQuantity(itemId: Long, requestDto: CartItemRequestDto): CartResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cartItem = cartItemRepository.findByIdAndUserEmail(itemId, email)
            ?: throw ResourceNotFoundException("Cart item not found with id: $itemId")

        val product = cartItem.product
            ?: throw ResourceNotFoundException("Product associated with cart item not found")

       validateQuantityStock(requestDto.quantity, product.stock)

        cartItem.quantity = requestDto.quantity
        cartItemRepository.save(cartItem)

        val cartId = cartItem.cart?.id
            ?: throw ResourceNotFoundException("Cart not found for item: $itemId")

        return cartRepository.findById(cartId).orElseThrow {
            ResourceNotFoundException("Cart not found")
        }.toResponseDto()
    }

    /**
     * Removes a single cart item identified by [itemId] from the authenticated
     * user's cart.  Ownership is verified — a user cannot delete items that
     * belong to another user's cart.
     * Throws [ResourceNotFoundException] if the item does not exist or does
     * not belong to the current user.
     * Returns the updated [CartResponseDto] with recalculated totals.
     */
    @Transactional
    fun removeCartItem(itemId: Long): CartResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cartItem = cartItemRepository.findByIdAndUserEmail(itemId, email)
            ?: throw ResourceNotFoundException("Cart item not found with id: $itemId")

        val cartId = cartItem.cart?.id
            ?: throw ResourceNotFoundException("Cart not found for item: $itemId")

        cartItemRepository.delete(cartItem)

        return cartRepository.findById(cartId).orElseThrow {
            ResourceNotFoundException("Cart not found")
        }.toResponseDto()
    }

    private fun validateQuantityStock(requestStock: Int, productStock: Int) {

        if (requestStock > productStock) {
            throw BusinessValidationException("Requested quantity (${requestStock}) exceeds available stock (${productStock})")
        }
    }
}

