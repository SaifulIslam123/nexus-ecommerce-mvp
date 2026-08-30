package com.ecommerce.mvp.modules.payment.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.PaymentFailedException
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
import com.ecommerce.mvp.modules.product.service.ProductService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionInterceptor

@Service
class PaymentService(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val productService: ProductService
) {


    @Transactional
    fun verifyPayment(requestDto: PaymentVerifyRequestDto): PaymentResponseDto {

        if (requestDto.orderId == null || requestDto.orderId <= 0) {
            throw BusinessValidationException("Invalid orderId: ${requestDto.orderId}")
        }

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(requestDto.orderId, email)
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
            }

            // In PaymentService — system received a FAILED response from payment gateway
            PaymentStatus.FAILED.name -> {
                order.status = OrderStatus.FAILED  // system-driven
                payment.status = PaymentStatus.FAILED
                productService.restoreStockAtomic(order.orderItems)  // restore stock for cancelled order
                throw PaymentFailedException(requestDto.failedReason ?: "Payment failed, try again later")
            }


            // In your PaymentService — user or system deliberately cancelled
            PaymentStatus.CANCELLED.name -> {
                order.status = OrderStatus.CANCELLED  // intentional, not an error
                payment.status = PaymentStatus.CANCELLED
                productService.restoreStockAtomic(order.orderItems)  // restore stock for cancelled order
            }

            else -> throw BusinessValidationException("Invalid payment status: ${requestDto.status}")
        }

        payment.method = requestDto.paymentMethod
        payment.transactionId = requestDto.transactionId
        payment.amount = requestDto.totalAmount
        payment.order = order
        return paymentRepository.save(payment).toResponseDto()
    }

}