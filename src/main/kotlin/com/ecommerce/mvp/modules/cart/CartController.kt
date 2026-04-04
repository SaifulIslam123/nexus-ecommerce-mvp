package com.ecommerce.mvp.modules.cart

import com.ecommerce.mvp.modules.cart.model.dto.CartItemRequestDto
import com.ecommerce.mvp.modules.cart.model.dto.CartResponseDto
import com.ecommerce.mvp.modules.cart.service.CartService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService
) {

    @PostMapping
    fun createCartForUser(): CartResponseDto {
        return cartService.createCartForUser()
    }

    /**
     * GET /api/cart
     *
     * Returns the authenticated user's current cart,
     * including all items and calculated totals.
     */
    @GetMapping
    fun getCart(): CartResponseDto {
        return cartService.getCart()
    }

    @PostMapping("/add-to-cart")
    fun addCart(@Valid @RequestBody cartItemRequestDto: CartItemRequestDto): CartResponseDto {
        return cartService.addItemToCart(cartItemRequestDto)
    }

    /**
     * PUT /api/cart/items/{itemId}
     *
     * Updates the quantity of a specific item in the authenticated user's cart.
     * Returns the updated cart with recalculated totals.
     */
    @PutMapping("/items/{itemId}")
    fun updateCartItem(
        @PathVariable itemId: Long,
        @Valid @RequestBody updateRequest: CartItemRequestDto
    ): CartResponseDto {
        return cartService.updateCartItemQuantity(itemId, updateRequest)
    }

    /**
     * DELETE /api/cart/items/{itemId}
     *
     * Removes the specified item from the authenticated user's cart.
     * Returns the updated cart with recalculated totals.
     */
    @DeleteMapping("/items/{itemId}")
    fun removeCartItem(@PathVariable itemId: Long): CartResponseDto {
        return cartService.removeCartItem(itemId)
    }

    /**
     * DELETE /api/cart
     *
     * Removes ALL items from the authenticated user's cart at once.
     * Returns the emptied cart (totalItems = 0, totalPrice = 0).
     */
    @DeleteMapping
    fun clearCart(): CartResponseDto {
        return cartService.clearCart()
    }
}

