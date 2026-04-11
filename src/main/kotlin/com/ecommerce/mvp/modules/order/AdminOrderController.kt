package com.ecommerce.mvp.modules.order

import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ShipOrderRequestDto
import com.ecommerce.mvp.modules.order.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(
    private val orderService: OrderService
) {
    // Expected date format 2026-04-10
    @GetMapping("/byDate")
    fun getOrdersByDate(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate? = null
    ): List<OrderResponseDto> {
        return orderService.getOrdersByDate(startDate, endDate)
    }

    /**
     * PUT /api/orders/{id}/processing
     *
     * Admin operation. Transitions a CONFIRMED order to PROCESSING,
     * signalling that the warehouse has started preparing the shipment.
     *
     * Allowed transition:  CONFIRMED → PROCESSING
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 Conflict if the order is not in CONFIRMED status.
     */
    @PutMapping("/{id}/processing")
    fun markAsProcessing(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsProcessing(id)
    }

    /**
     * PUT /api/orders/{id}/shipped
     *
     * Admin/courier operation. Transitions a PROCESSING order to SHIPPED
     * and creates the shipment record with the carrier's tracking number.
     *
     * Allowed transition:  PROCESSING → SHIPPED
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 Conflict if the order is not in PROCESSING status.
     */
    @PutMapping("/{id}/shipped")
    fun markAsShipped(
        @PathVariable id: Long,
        @Valid @RequestBody requestDto: ShipOrderRequestDto
    ): OrderResponseDto {
        return orderService.markAsShipped(id, requestDto)
    }

    /**
     * PUT /api/admin/orders/{id}/out-for-delivery
     *
     * Admin/courier operation. Transitions a SHIPPED order to OUT_FOR_DELIVERY,
     * signalling that the package is with the local courier for final delivery.
     *
     * Allowed transition:  SHIPPED → OUT_FOR_DELIVERY
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 Conflict if the order is not in SHIPPED status.
     */
    @PutMapping("/{id}/out-for-delivery")
    fun markAsOutForDelivery(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsOutForDelivery(id)
    }

    /**
     * PUT /api/admin/orders/{id}/delivered
     *
     * Admin/courier operation. Transitions an OUT_FOR_DELIVERY order to DELIVERED,
     * signalling that the package has been successfully received by the customer.
     *
     * Allowed transition:  OUT_FOR_DELIVERY → DELIVERED
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 Conflict if the order is not in OUT_FOR_DELIVERY status.
     */
    @PutMapping("/{id}/delivered")
    fun markAsDelivered(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsDelivered(id)
    }

}