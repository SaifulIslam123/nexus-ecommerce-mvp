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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartItemRepository: CartItemRepository
) {

    init {
        println("DEBUG REPO CLASS: " + this.orderRepository.javaClass.name);
    }

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

    /**
     * Returns every order that belongs to the currently authenticated user,
     * sorted from newest to oldest.
     * Throws [ResourceNotFoundException] if no authenticated user is found.
     */
    @Transactional(readOnly = true)
    fun getMyOrders(): List<OrderResponseDto> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        return orderRepository.findAllByUserEmail(email)
            .map { it.toResponseDto() }
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
    fun toPayOrder( requestDto: ToPayOrderRequest): OrderResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cartItem =
            cartItemRepository.findByIdAndUserEmailAndUserAddressId(requestDto.cartItemId!!, email, requestDto.addressId!!)
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

        val order = orderRepository.findByOrderId(orderId)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status != OrderStatus.PROCESSING) {
            throw BusinessValidationException(
                "Order can only move to SHIPPED from PROCESSING status. Current status: ${order.status}"
            )
        }

        order.shipment?.let {
            it.trackingId = TrackingIdGenerator.generateTrackingId()
            it.status = OrderStatus.SHIPPED.name
        }
        order.status = OrderStatus.SHIPPED

        return order.toResponseDto()
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

    private fun validateCartItemForCheckout(isActive: Boolean, quantity: Int, stock: Int) {
        if (!isActive)
            throw BusinessValidationException("This product is currently unavailable for purchase in order")
        if (quantity > stock)
            throw BusinessValidationException("Requested quantity ($quantity) exceeds available stock ($stock)")
    }

    //TODO: Handle multiple cart items
    /*fun initiateOrders(cartItemRequestDtoList: List<CartItemRequestDto>): List<Order> {

    }*/


    /** Admin will use this for getting specific date orders**/
    // Service method
    fun getOrdersByDate(
        startDate: LocalDate,
        endDate: LocalDate? = null,
    ): List<OrderResponseDto> {
        // Start of day in UTC:  2026-04-10T00:00:00Z
        val start: Instant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant()

        endDate?.let {
            val end: Instant = it.atStartOfDay(ZoneOffset.UTC).toInstant()
            return orderRepository.findByOrderDateBetween(start, end)
                .map { it.toResponseDto() }
        } ?: run {
            val end: Instant = startDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
            return orderRepository.findByOrderDateBetween(start, end)
                .map { it.toResponseDto() }
        }
    }

    /** Admin will use this for getting specific order status orders**/
    fun getOrderByStatus(status:OrderStatus): List<OrderResponseDto> {

        return orderRepository.findByStatus(status).map { it.toResponseDto() }
    }

    /** Runs every hour */
    @Scheduled(cron = "0 0 0 * * *")
    fun deleteAllExpiredReceivedOrders() {
        orderRepository.deleteAllExpiredReceivedOrders()
    }
}



