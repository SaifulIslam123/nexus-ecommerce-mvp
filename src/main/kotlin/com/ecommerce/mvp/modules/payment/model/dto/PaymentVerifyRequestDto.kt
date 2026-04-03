package com.ecommerce.mvp.modules.payment.model.dto

import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class PaymentVerifyRequestDto(
    @field:NotNull(message = "Payment method required")
    val paymentMethod: String? ,
    @field:NotNull(message = "Order ID not found")
    val orderId: Long? ,
    @field:NotNull(message = "TransactionId ID not found")
    val transactionId: String? ,
    @field:NotNull(message = "Payment staus not found")
    val status: String? ,
    @field:NotNull(message = "Payment staus not found")
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    )

