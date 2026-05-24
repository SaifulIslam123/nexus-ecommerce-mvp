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


    fun findById(id: Long): Order? {
        return orderRepository.findById(id).orElse(null)
    }

    fun findAll(): List<Order> {
        return orderRepository.findAll()
    }

    fun save(order: Order): Order {
        return orderRepository.save(order)
    }

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

    /**
     * Returns the full details of a single order identified by [orderId].
     * The order is fetched only when it belongs to the currently authenticated
     * user, so one user can never view another user's order.
     * Throws [ResourceNotFoundException] if the order does not exist or does
     * not belong to the current user.
     */
    @Transactional(readOnly = true)
    fun getOrderById(orderId: Long): OrderResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        return order.toResponseDto()
    }

    @Transactional
    fun cancelOrder(orderId: Long): OrderResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status == OrderStatus.TO_PAY || order.status == OrderStatus.CONFIRMED) {
            order.status = OrderStatus.CANCELLED

            order.orderItems.forEach { order ->
                order.product?.let { it.stock += order.quantity }
            }
            return order.toResponseDto()
        } else {
            throw BusinessValidationException("Cannot cancel an order that has already been ${order.status}")
        }
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

    /**
     * Transitions an order from [OrderStatus.CONFIRMED] to [OrderStatus.PROCESSING].
     *
     * This is an admin operation — it signals that the warehouse has started
     * picking, packing, or manufacturing the items for the given order.
     *
     * Allowed transition:  CONFIRMED → PROCESSING
     *
     * Throws [ResourceNotFoundException] if no order with [orderId] exists.
     * Throws [BusinessValidationException] if the order is not in CONFIRMED status.
     */
    @Transactional
    fun markAsProcessing(orderId: Long): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { throw ResourceNotFoundException("Order not found with id: $orderId") }


        if (order.status != OrderStatus.CONFIRMED) {
            throw BusinessValidationException(
                "Order can only move to PROCESSING from CONFIRMED status. Current status: ${order.status}"
            )
        }

        order.status = OrderStatus.PROCESSING

        return order.toResponseDto()
    }

    /**
     * Transitions an order from [OrderStatus.PROCESSING] to [OrderStatus.SHIPPED]
     * and creates the associated [Shipment] record for the order.
     *
     * This is an admin/courier operation — it signals that the package has been
     * handed over to the shipping carrier with a tracking number.
     *
     * Allowed transition:  PROCESSING → SHIPPED
     *
     * Because [Order.shipment] is declared with [CascadeType.ALL], assigning the
     * new [Shipment] to the managed [Order] entity is enough — Hibernate will
     * INSERT the shipment row automatically when the transaction commits,
     * without needing a separate shipment repository call.
     *
     * Throws [ResourceNotFoundException] if no order with [orderId] exists.
     * Throws [BusinessValidationException] if the order is not in PROCESSING status.
     */
    @Transactional
    fun markAsShipped(orderId: Long): OrderResponseDto {

        val order = orderRepository.findByIdWithShipment(orderId)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status != OrderStatus.PROCESSING) {
            throw BusinessValidationException(
                "Order can only move to SHIPPED from PROCESSING status. Current status: ${order.status}"
            )
        }
        setOrderToShipped(order)
        order.status = OrderStatus.SHIPPED

        return order.toResponseDto()
    }

    private fun setOrderToShipped(order: Order) {

        order.shipment?.let {
            it.trackingId = TrackingIdGenerator.generateTrackingId()
        }

    }

    /**
     * Transitions an order from [OrderStatus.DELIVERED] to [OrderStatus.RECEIVED].
     *
     * This is user operation — it signals that the package has been
     * successfully received by the customer from courier.
     *
     * Allowed transition:  DELIVERED → RECEIVED
     *
     * Throws [ResourceNotFoundException] if no order with [orderId] exists.
     * Throws [BusinessValidationException] if the order is not in OUT_FOR_DELIVERY status.
     */
    @Transactional
    fun markAsReceived(orderId: Long): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { throw ResourceNotFoundException("Order not found with id: $orderId") }

        if (order.status != OrderStatus.DELIVERED) {
            throw BusinessValidationException(
                "Order can only move to RECEIVED from DELIVERED status. Current status: ${order.status}"
            )
        }

        order.status = OrderStatus.RECEIVED
        // Keep the Shipment status string in sync with the order status
        //order.shipment?.status = OrderStatus.DELIVERED.name

        return order.toResponseDto()
    }

    /**
     * Transitions an order from [OrderStatus.RECEIVED] to [OrderStatus.RETURNED].
     *
     * This is a user operation — it signals that the customer wants to return
     * the package they already confirmed as received.
     *
     * Allowed transition:  RECEIVED → RETURNED
     *
     * Throws [ResourceNotFoundException] if no order with [orderId] exists or
     * does not belong to the currently authenticated user.
     * Throws [BusinessValidationException] if the order is not in RECEIVED status.
     */
    @Transactional
    fun markAsReturned(orderId: Long): OrderResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status != OrderStatus.RECEIVED) {
            throw BusinessValidationException(
                "Order can only move to RETURNED from RECEIVED status. Current status: ${order.status}"
            )
        }

        order.status = OrderStatus.RETURNED

        return order.toResponseDto()
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
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

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
             setOrderToRefunded(order)
            }
            OrderStatus.SHIPPED -> {
                setOrderToShipped(order)
            }
            else -> { /* no extra side effects */ }
        }

        return order.toResponseDto()
    }

    // Defines which statuses an order may transition INTO from a given status.
    private val allowedTransitions: Map<OrderStatus, Set<OrderStatus>> = mapOf(
        OrderStatus.TO_PAY          to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.FAILED),
        OrderStatus.CONFIRMED       to setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
        OrderStatus.PROCESSING      to setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED         to setOf(OrderStatus.OUT_FOR_DELIVERY),
        OrderStatus.OUT_FOR_DELIVERY to setOf(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED       to setOf(OrderStatus.RETURNED),
        OrderStatus.RETURNED        to setOf(OrderStatus.REFUNDED)
    )

    @Transactional
    fun markAsRefunded(orderId: Long): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

        if (order.status != OrderStatus.RETURNED) {
            throw BusinessValidationException(
                "Order can only move to REFUNDED from RETURNED status. Current status: ${order.status}"
            )
        }

        // 1. Update order status
        order.status = OrderStatus.REFUNDED
        setOrderToRefunded(order)


        return order.toResponseDto()
    }

    private fun setOrderToRefunded(order: Order) {

        // 2. Restore stock for every item
        order.orderItems.forEach { item ->
            item.product?.let { it.stock += item.quantity }
        }

        // 3. Update payment status
        order.payment?.status = PaymentStatus.REFUNDED
    }

}



