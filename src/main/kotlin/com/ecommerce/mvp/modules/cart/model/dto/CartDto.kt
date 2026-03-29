package com.ecommerce.mvp.modules.cart.model.dto

import com.ecommerce.mvp.common.AppConstant.MAX_Address
import com.ecommerce.mvp.common.AppConstant.MIN_CART_ITEM_QUANTITY
import com.ecommerce.mvp.modules.cart.model.entity.Cart
import com.ecommerce.mvp.modules.cart.model.entity.CartItem
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
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
    id = this.id ?: -1,
    productId = this.product?.id ?: -1,
    productName = this.product?.name ?: "",
    unitPrice = this.price,
    quantity = this.quantity,
    subtotal = this.price.multiply(BigDecimal(this.quantity))
)

// ── Cart ─────────────────────────────────────────────────────────────────────

data class CartResponseDto(
    val id: Long,
    val items: List<CartItemResponseDto>,
    val totalItems: Int,          // sum of all quantities
    val totalPrice: BigDecimal    // sum of all subtotals
)

fun Cart.toResponseDto(): CartResponseDto {
    val itemDtos = this.cartItems.map { it.toResponseDto() }
    return CartResponseDto(
        id = this.id ?: -1,
        items = itemDtos,
        totalItems = itemDtos.sumOf { it.quantity },
        totalPrice = itemDtos.fold(BigDecimal.ZERO) { acc, item -> acc + item.subtotal }
    )
}

data class CartItemRequestDto(
    @field:NotNull(message = "Product Id must not be null")
    val productId: Long = -1,
    @field:NotNull(message = "Quantity must not be null")
    @field:Min(value = MIN_CART_ITEM_QUANTITY.toLong(), message = "Quantity must be at least $MIN_CART_ITEM_QUANTITY")
    val quantity: Int = -1
)

