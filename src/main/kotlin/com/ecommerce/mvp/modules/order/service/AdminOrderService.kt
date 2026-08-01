package com.ecommerce.mvp.modules.order.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset


@Service("adminOrderService")
class AdminOrderService(
    private val orderRepository: OrderRepository,
) : OrderService(orderRepository) {

    fun deleteById(id: Long) {
        orderRepository.deleteById(id)
    }

    //TODO: Change to DB-Level pagination
    @Transactional(readOnly = true)
    override fun getMyOrders(page: Int, size: Int): Page<OrderResponseDto> {
        return super.getMyOrders(page, size)
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

    /** Admin will use this for getting specific date orders**/
    // Service method
    @Transactional
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
    @Transactional
    fun getOrderByStatus(status: OrderStatus): List<OrderResponseDto> {

        return orderRepository.findByStatus(status).map { it.toResponseDto() }
    }

    @Transactional
    fun adminUpdateOrderStatus(orderId: Long, status: OrderStatus): OrderResponseDto {

        val order = orderRepository.findOrderById(orderId)
            ?: throw ResourceNotFoundException("Order not found with id: $orderId")

        if (order.status == OrderStatus.REFUNDED) {
            throw BusinessValidationException("Cannot update status of a refunded order.")
        }

        return updateOrderStatus(order, status)
    }

}