package com.ecommerce.mvp.modules.cart.model.dto

import com.ecommerce.mvp.modules.cart.model.entity.Cart
import com.ecommerce.mvp.modules.cart.model.entity.CartItem
import java.math.BigDecimal

// ── Cart Item ────────────────────────────────────────────────────────────────

data class CartItemResponseDto(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
)

fun CartItem.toResponseDto() = CartItemResponseDto(
    id         = this.id ?: -1,
    productId  = this.product?.id ?: -1,
    productName= this.product?.name ?: "",
    unitPrice  = this.price,
    quantity   = this.quantity,
    subtotal   = this.price.multiply(BigDecimal(this.quantity))
)

// ── Cart ─────────────────────────────────────────────────────────────────────

data class CartResponseDto(
    val cartId      : Long,
    val items       : List<CartItemResponseDto>,
    val totalItems  : Int,          // sum of all quantities
    val totalPrice  : BigDecimal    // sum of all subtotals
)

fun Cart.toResponseDto(): CartResponseDto {
    val itemDtos = this.cartItems.map { it.toResponseDto() }
    return CartResponseDto(
        cartId     = this.id ?: -1,
        items      = itemDtos,
        totalItems = itemDtos.sumOf { it.quantity },
        totalPrice = itemDtos.fold(BigDecimal.ZERO) { acc, item -> acc + item.subtotal }
    )
}

