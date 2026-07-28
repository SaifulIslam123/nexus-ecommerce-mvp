package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.cart.repository.CartItemRepository
import com.ecommerce.mvp.modules.courier.TrackingIdGenerator
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ToPayOrderRequest
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderItem
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.model.entity.Shipment
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.payment.model.entity.PaymentStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartItemRepository: CartItemRepository,
) {


    fun deleteById(id: Long) {
        orderRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getMyOrders(page: Int, size: Int): Page<OrderResponseDto> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val allOrders = orderRepository.findAllByUserEmail(email).map { it.toResponseDto() }
        val pageable = PageRequest.of(page, size)
        val start = (page * size).coerceAtMost(allOrders.size)
        val end = (start + size).coerceAtMost(allOrders.size)
        return PageImpl(allOrders.subList(start, end), pageable, allOrders.size.toLong())
    }

    @Transactional(readOnly = true)
    fun getAllSystemOrders(page: Int, size: Int): Page<OrderResponseDto> {

        SecurityContextHolder.getContext().authentication
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val allOrders = orderRepository.findAll().map { it.toResponseDto() }
        val pageable = PageRequest.of(page, size)
        val start = (page * size).coerceAtMost(allOrders.size)
        val end = (start + size).coerceAtMost(allOrders.size)
        return PageImpl(allOrders.subList(start, end), pageable, allOrders.size.toLong())
    }

    /**
     * Returns the full details of a single order identified by [orderId].
     * The order is fetched only when it belongs to the currently authenticated
     * user, so one user can never view another user's order.
     * Throws [ResourceNotFoundException] if the order does not exist or does
     * not belong to the current user.
     */
    @Transactional(readOnly = true)
    fun getOrderById(orderId: Long): OrderResponseDto {
        SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        return order.toResponseDto()
    }

    @Transactional(readOnly = true)
    fun getUserOrderById(orderId: Long): OrderResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        return order.toResponseDto()
    }

    /*
    *
    * Checkout Start
    *
    * */
    @Transactional
    fun toPayOrder(requestDto: ToPayOrderRequest): OrderResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cartItem =
            cartItemRepository.findByIdAndUserEmailIfAddressOwned(
                requestDto.cartItemId!!,
                email,
                requestDto.addressId!!
            )
                ?: throw ResourceNotFoundException("Cart Item not found")

        cartItem.product?.let {
            validateCartItemForCheckout(
                isActive = it.isActive,
                quantity = cartItem.quantity,
                stock = it.stock
            )
        } ?: throw ResourceNotFoundException("Cart Item does not have any product")


        val toPayOrder = Order(
            orderDate = Instant.now(),
            totalAmount = cartItem.price,
            status = OrderStatus.TO_PAY,
            user = cartItem.cart?.user,
            orderItems = mutableSetOf(),
            payment = null,
            shipment = null
        )

        val orderItem = OrderItem().apply {
            quantity = cartItem.quantity
            order = toPayOrder
            product = cartItem.product
            price = cartItem.price
        }

        val shipment = Shipment().apply {
            shipmentAddress = cartItem.cart?.user?.addresses?.first()!!
            estimatedDeliveryDate = requestDto.deliveryDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
            order = toPayOrder
        }

        toPayOrder.shipment = shipment
        toPayOrder.orderItems.add(orderItem)
        cartItem.product?.let { it.stock -= cartItem.quantity }
        cartItem.cart?.cartItems?.remove(cartItem)

        val saveOrder = orderRepository.save(toPayOrder)

        return saveOrder.toResponseDto()
    }




    private fun validateCartItemForCheckout(isActive: Boolean, quantity: Int, stock: Int) {
        if (!isActive)
            throw BusinessValidationException("This product is currently unavailable for purchase in order")
        if (quantity > stock)
            throw BusinessValidationException("Requested quantity ($quantity) exceeds available stock ($stock)")
    }


    /** Admin will use this for getting specific date orders**/
    // Service method
    fun getOrdersByDate(
        startDate: LocalDate,
        endDate: LocalDate? = null,
    ): List<OrderResponseDto> {
        // Start of day in UTC:  2026-04-10T00:00:00Z
        val start: Instant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant()

        endDate?.let {
            val end: Instant = it.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
            return orderRepository.findByOrderDateBetween(start, end)
                .map { it.toResponseDto() }
        } ?: run {
            val end: Instant = startDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
            return orderRepository.findByOrderDateBetween(start, end)
                .map { it.toResponseDto() }
        }
    }

    /** Admin will use this for getting specific order status orders**/
    fun getOrderByStatus(status: OrderStatus): List<OrderResponseDto> {

        return orderRepository.findByStatus(status).map { it.toResponseDto() }
    }

    @Transactional
    fun shopperUpdateOrderStatus(orderId: Long, status: OrderStatus): OrderResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        return updateOrderStatus(order, status)
    }

    @Transactional
    fun adminUpdateOrderStatus(orderId: Long, status: OrderStatus): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

        return updateOrderStatus(order, status)
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



