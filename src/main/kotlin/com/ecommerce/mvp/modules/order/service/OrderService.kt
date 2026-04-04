package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.cart.model.entity.CartItem
import com.ecommerce.mvp.modules.cart.repository.CartItemRepository
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderItem
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

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

            order.orderItems.forEach { item ->
                item.product?.let { it.stock += item.quantity }
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
    fun toPayOrder(cartItemId: Long): OrderResponseDto {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val cartItem = cartItemRepository.findByIdAndUserEmail(cartItemId, email)
            ?: throw ResourceNotFoundException("Cart Item not found")


        cartItem.product?.let {
            validateCartItemForCheckout(
                isActive = it.isActive,
                quantity = cartItem.quantity,
                stock = it.stock
            )
        } ?: throw ResourceNotFoundException("Cart Item does not have any product")



        val saveOrder = Order(
            orderDate = Date(),
            totalAmount = cartItem.price,
            status = OrderStatus.TO_PAY,
            user = cartItem.cart?.user,
            orderItems = mutableSetOf(),
            payment = null,
            shipment = null
        )

        val orderItem = OrderItem().apply {
            quantity = cartItem.quantity
            order = saveOrder
            product = cartItem.product
            price = cartItem.price
        }

        saveOrder.orderItems.add(orderItem)
        cartItem.product?.let { it.stock -= cartItem.quantity }
        cartItem.cart?.cartItems?.remove(cartItem)

        return orderRepository.save(saveOrder).toResponseDto()
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


}



