package com.ecommerce.mvp.modules.cart

import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.cart.model.dto.CartItemRequestDto
import com.ecommerce.mvp.modules.cart.model.dto.CartResponseDto
import com.ecommerce.mvp.modules.cart.service.CartService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService
) {

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

    @PostMapping
    fun addCart(@Valid @RequestBody cartItemRequestDto: CartItemRequestDto) {
        cartService.addItemToCart(cartItemRequestDto)
    }
}

