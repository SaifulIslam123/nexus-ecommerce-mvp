package com.ecommerce.mvp.modules.payment

import com.ecommerce.mvp.modules.order.model.dto.PaymentResponseDto
import com.ecommerce.mvp.modules.payment.model.dto.PaymentVerifyRequestDto
import com.ecommerce.mvp.modules.payment.service.PaymentService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(private val paymentService: PaymentService) {



    @PostMapping("/webhook")
    fun verifyOrder(@RequestBody requestDto: PaymentVerifyRequestDto): PaymentResponseDto {
        return paymentService.verifyPayment(requestDto)
    }
}