package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.cart.model.dto.CartItemRequestDto
import com.ecommerce.mvp.modules.cart.repository.CartItemRepository
import com.ecommerce.mvp.modules.cart.repository.CartRepository
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.product.model.entity.Product
import com.ecommerce.mvp.modules.product.repository.ProductRepository
import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository
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
    fun cancelOrder(orderId: Long) {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        val order = orderRepository.findByIdAndUserEmail(orderId, email)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
            order.status = OrderStatus.CANCELLED

            order.orderItems.forEach {
                it.product.stock += it.quantity
            }

        } else {
            throw IllegalStateException("Cannot cancel an order that has already been ${order.status}")
        }
    }


    /*
    *
    * Checkout Start
    *
    * */
    @Transactional
    fun initiateOrder(cartItemId: Long) {

        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated user not found")

        /*val product = productRepository.findById(cartItemRequestDto.productId )
            .orElseThrow { ResourceNotFoundException("Product not found with id: ${cartItemRequestDto.productId}") }*/

        val cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow { ResourceNotFoundException("Cart Item not found ") }

        if (!cartItem.product.isActive) {
            throw BusinessValidationException("This product is currently unavailable for purchase in order")
        }
        if (cartItem.quantity > cartItem.product.stock) {
            throw BusinessValidationException("Requested quantity (${cartItem.quantity}) exceeds available stock (${cartItem.product.stock})")
        }

        val order = Order().apply {
            this.user = user
            orderDate = Date()
            totalAmount = cartItem.price
            status = OrderStatus.PENDING
        }

        cartItem.product.stock -= cartItem.quantity

        orderRepository.save(order)


    }

    //TODO: Handle multiple cart items
    /*fun initiateOrders(cartItemRequestDtoList: List<CartItemRequestDto>): List<Order> {

    }*/


    @Transactional
    fun insertDummyOrders(): List<Order> {
        // First, get some existing users to associate with orders
        val users = userRepository.findAll()

        if (users.isEmpty()) {
            println("No users found. Please create users first before creating orders.")
            return emptyList()
        }

        val dummyOrders = mutableListOf<Order>()

        // Create orders and assign them to users (cycling through available users)
        dummyOrders.add(Order().apply {
            orderDate = Date()
            totalAmount = BigDecimal("250.00")
            status = OrderStatus.PENDING
            user = users[0 % users.size]
        })

        dummyOrders.add(Order().apply {
            orderDate = Date()
            totalAmount = BigDecimal("150.50")
            status = OrderStatus.PROCESSING
            user = users[1 % users.size]
        })

        dummyOrders.add(Order().apply {
            orderDate = Date()
            totalAmount = BigDecimal("500.00")
            status = OrderStatus.DELIVERED
            user = users[2 % users.size]
        })

        dummyOrders.add(Order().apply {
            orderDate = Date()
            totalAmount = BigDecimal("350.75")
            status = OrderStatus.SHIPPED
            user = users[0 % users.size]
        })

        val savedOrders = orderRepository.saveAll(dummyOrders)
        println("Successfully inserted ${savedOrders.size} dummy orders")
        return savedOrders
    }

   // @Transactional
    fun testOrder(){
       /*val orders = orderRepository.findById(2)
        println("Order_Details: ${orders.get().totalAmount}, ${orders.get().user?.name}")*/

        val orders = orderRepository.findAll()
        /*for(order in orders){
            println("Order_ID: ${order.id}, Order_TotalAmount: ${order.totalAmount}, User_Name: ${order.user?.name}")
        }*/
    }


}
