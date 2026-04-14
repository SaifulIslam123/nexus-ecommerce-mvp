package com.ecommerce.mvp.modules.payment.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.order.model.dto.PaymentResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.payment.model.dto.PaymentVerifyRequestDto
import com.ecommerce.mvp.modules.payment.model.entity.Payment
import com.ecommerce.mvp.modules.payment.model.entity.PaymentStatus
import com.ecommerce.mvp.modules.payment.repository.PaymentRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) {


    @Transactional
    fun verifyPayment(requestDto: PaymentVerifyRequestDto): PaymentResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(requestDto.orderId!!, email)
            ?: throw ResourceNotFoundException("Order not found")

        if (order.totalAmount != requestDto.totalAmount) {
            throw BusinessValidationException("Total amount should be equal to request amount")
        }

        val payment = Payment()

        when (requestDto.status) {
            PaymentStatus.TO_PAY.name -> {
                order.status = OrderStatus.TO_PAY
                payment.status = PaymentStatus.TO_PAY
            }

            PaymentStatus.COMPLETED.name -> {
                order.status = OrderStatus.CONFIRMED
                payment.status = PaymentStatus.COMPLETED

                // Additional logic for successful payment (e.g., send confirmation email)
                //deductProductForOrder(order)
            }

            // In PaymentService — system received a FAILED response from payment gateway
            PaymentStatus.FAILED.name -> {
                order.status = OrderStatus.FAILED  // system-driven
                payment.status = PaymentStatus.FAILED
            }


            // In your PaymentService — user or system deliberately cancelled
            PaymentStatus.CANCELLED.name -> {
                order.status = OrderStatus.CANCELLED  // intentional, not an error
                payment.status = PaymentStatus.CANCELLED
            }

            else -> throw BusinessValidationException("Invalid payment status: ${requestDto.status}")
        }

        payment.method = requestDto.paymentMethod
        payment.transactionId = requestDto.transactionId
        payment.amount = requestDto.totalAmount
        payment.order = order
        return paymentRepository.save(payment).let { it.toResponseDto() }
    }

}