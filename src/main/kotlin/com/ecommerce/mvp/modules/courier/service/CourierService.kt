package com.ecommerce.mvp.modules.courier.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.model.entity.Shipment
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourierService(
    private val orderRepository: OrderRepository
) {


    /**
     * Transitions an order from [OrderStatus.SHIPPED] to [OrderStatus.OUT_FOR_DELIVERY].
     *
     * This is an admin/courier operation — it signals that the package has been
     * picked up by the local delivery courier and is on its way to the customer.
     *
     * Allowed transition:  SHIPPED → OUT_FOR_DELIVERY
     *
     * The associated [Shipment] status string is also updated to keep it
     * in sync with the order status.
     *
     * Throws [ResourceNotFoundException] if no order with [orderId] exists.
     * Throws [BusinessValidationException] if the order is not in SHIPPED status.
     */
    @Transactional
    fun markAsOutForDelivery(orderId: Long): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { throw ResourceNotFoundException("Order not found with id: $orderId") }

        if (order.status != OrderStatus.SHIPPED) {
            throw BusinessValidationException(
                "Order can only move to OUT_FOR_DELIVERY from SHIPPED status. Current status: ${order.status}"
            )
        }

        order.status = OrderStatus.OUT_FOR_DELIVERY

        return order.toResponseDto()
    }

    /**
     * Transitions an order from [OrderStatus.OUT_FOR_DELIVERY] to [OrderStatus.DELIVERED].
     *
     * This is an admin/courier operation — it signals that the package has been
     * successfully received by the customer.
     *
     * Allowed transition:  OUT_FOR_DELIVERY → DELIVERED
     *
     * The associated [Shipment] status string is also updated to keep it
     * in sync with the order status.
     *
     * Throws [ResourceNotFoundException] if no order with [orderId] exists.
     * Throws [BusinessValidationException] if the order is not in OUT_FOR_DELIVERY status.
     */
    @Transactional
    fun markAsDelivered(orderId: Long): OrderResponseDto {

        val order = orderRepository.findById(orderId)
            .orElseThrow { throw ResourceNotFoundException("Order not found with id: $orderId") }

        if (order.status != OrderStatus.OUT_FOR_DELIVERY) {
            throw BusinessValidationException(
                "Order can only move to DELIVERED from OUT_FOR_DELIVERY status. Current status: ${order.status}"
            )
        }

        order.status = OrderStatus.DELIVERED

        return order.toResponseDto()
    }
}
