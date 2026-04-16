package com.ecommerce.mvp.modules.order.model.dto

import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderItem
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.payment.model.entity.Payment
import com.ecommerce.mvp.modules.order.model.entity.Shipment
import com.ecommerce.mvp.modules.payment.model.entity.PaymentStatus
import com.ecommerce.mvp.modules.user.model.dto.AddressDto
import com.ecommerce.mvp.modules.user.model.dto.toAddressDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

// ── Admin Status Update ───────────────────────────────────────────────────────

data class UpdateOrderStatusRequest(
    @field:NotNull(message = "status is required")
    val status: OrderStatus
)

// ── Order Item ────────────────────────────────────────────────────────────────

data class ToPayOrderRequest(
    @field:NotNull(message = "CartItem id is required")
    val cartItemId: Long?,
    @field:NotNull(message = "Delivery date is required")
    val deliveryDate: LocalDate?,
    @field:NotNull(message = "Order shipment address not found")
    val addressId: Long?,
)

data class OrderItemResponseDto(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
)

fun OrderItem.toResponseDto() = OrderItemResponseDto(
    id = this.id ?: -1,
    productId = this.product?.id ?: -1,
    productName = this.product?.name.toString(),
    unitPrice = this.price,
    quantity = this.quantity,
    subtotal = this.price.multiply(BigDecimal(this.quantity))
)

// ── Payment ───────────────────────────────────────────────────────────────────

data class PaymentResponseDto(
    val id: Long,
    val method: String?,
    val amount: BigDecimal,
    val transactionId: String?,
    val status: PaymentStatus?,
    val orderId: Long?,
)

fun Payment.toResponseDto() = PaymentResponseDto(
    id = this.id ?: -1,
    method = this.method,
    amount = this.amount,
    transactionId = this.transactionId,
    status = this.status,
    orderId = this.order?.id
)

// ── Shipment ──────────────────────────────────────────────────────────────────


data class ShipmentResponseDto(
    val id: Long,
    val trackingId: String?,
    val address: AddressDto?
)

fun Shipment.toResponseDto() = ShipmentResponseDto(
    id = this.id ?: -1,
    trackingId = this.trackingId,
    address = this.shipmentAddress?.toAddressDto()
)

// ── Order ─────────────────────────────────────────────────────────────────────

data class OrderResponseDto(
    val id: Long,
    val orderDate: Instant,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val items: List<OrderItemResponseDto>,
    val payment: PaymentResponseDto?,
    val shipment: ShipmentResponseDto?
)

fun Order.toResponseDto() = OrderResponseDto(
    id = this.id ?: -1,
    orderDate = this.orderDate,
    totalAmount = this.totalAmount,
    status = this.status,
    items = this.orderItems.map { it.toResponseDto() },
    payment = this.payment?.toResponseDto(),
    shipment = this.shipment?.toResponseDto()
)

