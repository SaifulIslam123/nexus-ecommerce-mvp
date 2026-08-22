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

        //val userCart = user.cart//cartRepository.findByUserEmail(email)
        user.cart?.let {
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

        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")
        user.cart?.let { userCart ->

            requestDto.productId?.let { productId ->
                val requestProduct = productRepository.findById(productId).orElseThrow { ResourceNotFoundException("Product not found with id: ${productId}") }

                validateProduct(requestDto.quantity, requestProduct.stock, requestProduct.isActive)

                val cartItem = CartItem().apply {
                    product = requestProduct
                    quantity = requestDto.quantity
                    price = requestProduct.price
                    cart = userCart
                }

                val savedCartItem = cartItemRepository.save(cartItem)
                userCart.cartItems.add(savedCartItem)
                return userCart.toResponseDto()

            } ?: run { throw BusinessValidationException("Product Id must not be null") }
        } ?: run { throw BusinessValidationException("No cart found for user: $email, need to create cart first for add item to cart") }

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

        validateProduct(requestDto.quantity, product.stock, product.isActive)

        cartItem.quantity = requestDto.quantity

        return cartItem.cart?.toResponseDto()
            ?: throw ResourceNotFoundException("Cart not found for item: $itemId")
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
    fun removeCartItem(itemId: Long) {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cartItem = cartItemRepository.findByIdAndUserEmail(itemId, email)
            ?: throw ResourceNotFoundException("Cart item not found with id: $itemId")

        cartItem.cart?.cartItems?.remove(cartItem) // orphanRemoval triggers DELETE for CartItem rows

        /*return cartItem.cart?.toResponseDto()
            ?: throw ResourceNotFoundException("Cart not found for item: $itemId")*/
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
    fun clearCart() {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cart = cartRepository.findByUserEmail(email)
            ?: throw ResourceNotFoundException("Cart not found for user: $email")

        cart.cartItems.clear()  // orphanRemoval triggers DELETE for all CartItem rows

        //return cart.toResponseDto()
    }

    private fun validateProduct(requestStock: Int, productStock: Int, isActive: Boolean) {

        if (!isActive) {
            throw BusinessValidationException("Currently this product is not active")
        }

        if (requestStock > productStock) {
            throw BusinessValidationException("Requested quantity (${requestStock}) exceeds available stock (${productStock})")
        }
    }

}

