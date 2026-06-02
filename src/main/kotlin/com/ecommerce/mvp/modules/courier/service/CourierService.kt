package com.ecommerce.mvp.modules.courier.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.courier.TrackingIdGenerator
import com.ecommerce.mvp.modules.courier.model.entity.Courier
import com.ecommerce.mvp.modules.courier.repository.CourierRepository
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CourierService(
    private val orderRepository: OrderRepository,
    private val courierRepository: CourierRepository,
) {

    companion object {
        private val COURIER_VISIBLE_STATUSES = setOf(
            OrderStatus.SHIPPED,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED,
        )
    }

    @Transactional(readOnly = true)
    fun getOrdersForCourier(status: OrderStatus?, page: Int, size: Int): Page<OrderResponseDto> {
        if (status != null && status !in COURIER_VISIBLE_STATUSES) {
            throw BusinessValidationException(
                "Courier cannot query orders with status $status. Allowed: ${COURIER_VISIBLE_STATUSES.joinToString()}"
            )
        }

        val statuses = if (status != null) listOf(status) else COURIER_VISIBLE_STATUSES.toList()
        val orders = orderRepository.findByStatusIn(statuses).map { it.toResponseDto() }

        val pageable = PageRequest.of(page, size)
        val start = (page * size).coerceAtMost(orders.size)
        val end = (start + size).coerceAtMost(orders.size)
        return PageImpl(orders.subList(start, end), pageable, orders.size.toLong())
    }

    @Transactional(readOnly = true)
    fun getOrderByIdForCourier(id: Long): OrderResponseDto {
        val order = orderRepository.findOrderByIdAdmin(id)
            ?: throw ResourceNotFoundException("Order not found with id: $id")
        return order.toResponseDto()
    }

    /**
     * Transitions SHIPPED → OUT_FOR_DELIVERY and creates a Courier audit record.
     * The Courier record captures tracking ID, shipment address, and acting user
     * (via BaseEntityAudit.createdBy).
     */
    @Transactional
    fun markAsOutForDelivery(orderId: Long): OrderResponseDto {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

        if (order.status != OrderStatus.SHIPPED) {
            throw BusinessValidationException(
                "Order can only move to OUT_FOR_DELIVERY from SHIPPED status. Current status: ${order.status}"
            )
        }

        val shipment = order.shipment
            ?: throw BusinessValidationException("Order $orderId has no shipment record")

        if (courierRepository.existsByOrderId(orderId)) {
            throw BusinessValidationException("Order $orderId is already being tracked as out for delivery")
        }

        order.status = OrderStatus.OUT_FOR_DELIVERY

        val addr = shipment.shipmentAddress
        val addressString = if (addr != null) {
            "${addr.street}, ${addr.city}, ${addr.zip}, ${addr.country}"
        } else ""

        val courierRecord = Courier().apply {
            this.orderId = orderId
            this.trackingId = shipment.trackingId ?: TrackingIdGenerator.generateTrackingId()
            this.shipmentDate = Instant.now()
            this.shipmentAddress = addressString
        }
        courierRepository.save(courierRecord)

        return order.toResponseDto()
    }

    /**
     * Transitions OUT_FOR_DELIVERY → DELIVERED.
     * Saves the existing Courier record unchanged so that BaseEntityAudit stamps
     * modifiedBy + modifiedDate with the acting courier's identity.
     */
    @Transactional
    fun markAsDelivered(orderId: Long): OrderResponseDto {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $orderId") }

        if (order.status != OrderStatus.OUT_FOR_DELIVERY) {
            throw BusinessValidationException(
                "Order can only move to DELIVERED from OUT_FOR_DELIVERY status. Current status: ${order.status}"
            )
        }

        order.status = OrderStatus.DELIVERED

        courierRepository.findByOrderId(orderId).ifPresent { courierRepository.save(it) }

        return order.toResponseDto()
    }
}
