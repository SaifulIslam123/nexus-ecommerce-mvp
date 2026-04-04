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

    @Transactional
    fun createCartForUser(): CartResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val user = userRepository.findByUserEmail(email)
            ?: throw ResourceNotFoundException("User not found with email: $email")

        val userCart = cartRepository.findByUserEmail(email)
        userCart?.let {
            throw BusinessValidationException("User cart already exists with id: ${it.id}")
        } ?: run {
            val cart = Cart(user = user)
           return cartRepository.save(cart).toResponseDto()
        }
    }

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

        requestDto.productId?.let { productId ->
            val product = productRepository.findById(productId)
                .orElseThrow { ResourceNotFoundException("Product not found with id: ${productId}") }

            validateQuantityStock(requestDto.quantity, product.stock)

            val email = SecurityContextHolder.getContext().authentication?.name
            val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")

            val cart = Cart(user = user)

            val cartItem = CartItem(
                product = product,
                quantity = requestDto.quantity,
                price = product.price,
                cart = cart
            )
            cart.cartItems.add(cartItem)

            val savedCart = cartRepository.save(cart)

            return savedCart.toResponseDto()
        } ?: run { throw BusinessValidationException("Product Id must not be null") }
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

    /**
     * Clears every item from the authenticated user's cart in one shot.
     * Because [Cart.cartItems] is declared with [orphanRemoval] = true,
     * calling [MutableSet.clear] on the collection and saving the cart is
     * enough — Hibernate will issue the DELETE statements for all child rows.
     * Throws [ResourceNotFoundException] if the user has no cart.
     * Returns the emptied [CartResponseDto] (totals will be 0).
     */
    @Transactional
    fun clearCart(): CartResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cart = cartRepository.findByUserEmail(email)
            ?: throw ResourceNotFoundException("Cart not found for user: $email")

        cart.cartItems.clear()  // orphanRemoval triggers DELETE for all CartItem rows

        return cartRepository.save(cart).toResponseDto()
    }

    private fun validateQuantityStock(requestStock: Int, productStock: Int) {

        if (requestStock > productStock) {
            throw BusinessValidationException("Requested quantity (${requestStock}) exceeds available stock (${productStock})")
        }
    }
}

