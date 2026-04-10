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
import java.math.BigDecimal
import java.time.Instant

// ── Order Item ────────────────────────────────────────────────────────────────

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

/**
 * Request body for the "mark as shipped" admin endpoint.
 * [trackingId] is the tracking number assigned by the carrier.
 */
data class ShipOrderRequestDto(
    @field:NotBlank(message = "Tracking ID must not be blank")
    val trackingId: String
)

data class ShipmentResponseDto(
    val id: Long,
    val status: String?,
    val trackingId: String?
)

fun Shipment.toResponseDto() = ShipmentResponseDto(
    id = this.id ?: -1,
    status = this.status,
    trackingId = this.trackingId
)

// ── Order ─────────────────────────────────────────────────────────────────────

data class OrderResponseDto(
    val id: Long,
    val orderDate: Instant,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val items: List<OrderItemResponseDto>,
    val payment: PaymentResponseDto?,
    val shipment: ShipmentResponseDto?,
    val shipmentAddress: AddressDto?
)

fun Order.toResponseDto() = OrderResponseDto(
    id = this.id ?: -1,
    orderDate = this.orderDate,
    totalAmount = this.totalAmount,
    status = this.status,
    items = this.orderItems.map { it.toResponseDto() },
    payment = this.payment?.toResponseDto(),
    shipment = this.shipment?.toResponseDto(),
    shipmentAddress = this.shipmentAddress?.toAddressDto()
)

