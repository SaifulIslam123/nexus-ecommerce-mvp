package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
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
            status = OrderStatus.COMPLETED
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
