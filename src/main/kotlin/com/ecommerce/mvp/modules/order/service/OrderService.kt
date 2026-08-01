package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.courier.TrackingIdGenerator
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.payment.model.entity.PaymentStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest


open class OrderService(private val orderRepository: OrderRepository) {


    //TODO: Change to DB-Level pagination 
    //@Transactional(readOnly = true)
    open fun getMyOrders(page: Int, size: Int): Page<OrderResponseDto> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val allOrders = orderRepository.findAllByUserEmail(email).map { it.toResponseDto() }
        val pageable = PageRequest.of(page, size)
        val start = (page * size).coerceAtMost(allOrders.size)
        val end = (start + size).coerceAtMost(allOrders.size)
        return PageImpl(allOrders.subList(start, end), pageable, allOrders.size.toLong())
    }


    /**
     * Admin: Transitions an order to any [newStatus] and notifies the customer
     * by email.  Basic guard rails are applied to avoid nonsensical transitions
     * (e.g., moving a DELIVERED order back to TO_PAY).
     *
     * Allowed transitions (from → to):
     *   TO_PAY       → CONFIRMED | CANCELLED | FAILED
     *   CONFIRMED    → PROCESSING | CANCELLED
     *   PROCESSING   → SHIPPED | CANCELLED
     *   SHIPPED      → OUT_FOR_DELIVERY
     *   OUT_FOR_DELIVERY → DELIVERED
     *   DELIVERED    → RETURNED
     *   RETURNED     → REFUNDED
     *
     * Throws [BusinessValidationException] for any disallowed transition.
     */
    @Transactional
    fun updateOrderStatus(order: Order, newStatus: OrderStatus): OrderResponseDto {

        val allowed: Set<OrderStatus> = allowedTransitions[order.status]
            ?: throw BusinessValidationException(
                "No transitions are allowed from status: ${order.status}"
            )

        if (newStatus !in allowed) {
            throw BusinessValidationException(
                "Cannot transition order from ${order.status} to $newStatus. " +
                        "Allowed next statuses: ${allowed.joinToString()}"
            )
        }

        order.status = newStatus

        // Side-effects that mirror dedicated transition methods
        when (newStatus) {
            OrderStatus.REFUNDED -> {

                //  Restore stock for every item
                order.orderItems.forEach { item ->
                    item.product?.let { it.stock += item.quantity }
                }

                //  Update payment status
                order.payment?.status = PaymentStatus.REFUNDED
            }

            OrderStatus.SHIPPED -> {

                order.shipment?.let {
                    it.trackingId = TrackingIdGenerator.generateTrackingId()
                }
            }

            OrderStatus.CANCELLED -> {
                order.orderItems.forEach { order ->
                    order.product?.let { it.stock += order.quantity }
                }
            }

            else -> { /* no extra side effects */
            }
        }

        return order.toResponseDto()
    }

    // Defines which statuses an order may transition INTO from a given status.
    private val allowedTransitions: Map<OrderStatus, Set<OrderStatus>> = mapOf(
        OrderStatus.TO_PAY to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.FAILED),
        OrderStatus.CONFIRMED to setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
        OrderStatus.PROCESSING to setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED to setOf(OrderStatus.OUT_FOR_DELIVERY),
        OrderStatus.OUT_FOR_DELIVERY to setOf(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED to setOf(OrderStatus.RETURNED, OrderStatus.RECEIVED),
        OrderStatus.RECEIVED to setOf(OrderStatus.RETURNED, OrderStatus.REFUNDED),
        OrderStatus.RETURNED to setOf(OrderStatus.REFUNDED)
    )


}



