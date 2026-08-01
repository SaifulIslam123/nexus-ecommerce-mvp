package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.cart.repository.CartItemRepository
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ToPayOrderRequest
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderItem
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.model.entity.Shipment
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset

@Service
class ShopperOrderService (
    private val orderRepository: OrderRepository,
    private val cartItemRepository: CartItemRepository,
) : OrderService(orderRepository) {

    //TODO: Change to DB-Level pagination
    @Transactional(readOnly = true)
    override fun getMyOrders(page: Int, size: Int): Page<OrderResponseDto> {
        return super.getMyOrders(page, size)
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

            if (!it.isActive)
                throw BusinessValidationException("This product is currently unavailable for purchase in order")
            if (cartItem.quantity > it.stock)
                throw BusinessValidationException("Requested quantity (${cartItem.quantity}) exceeds available stock (${it.stock})")

            // Atomic decrement — either succeeds or fails atomically at DB level
            val updated = cartItemRepository.decrementStockIfAvailable(
                it.id ?: 0,
                cartItem.quantity
            )
            if (updated == 0) {
                throw BusinessValidationException("Stock was just sold out, please try again")
            }
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
        cartItem.cart?.cartItems?.remove(cartItem)

        val saveOrder = orderRepository.save(toPayOrder)

        return saveOrder.toResponseDto()
    }

    @Transactional
    fun shopperUpdateOrderStatus(orderId: Long, status: OrderStatus): OrderResponseDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status == OrderStatus.CANCELLED) {
            throw BusinessValidationException("Cannot update status of a cancelled order.")
        }

        return updateOrderStatus(order, status)
    }

}