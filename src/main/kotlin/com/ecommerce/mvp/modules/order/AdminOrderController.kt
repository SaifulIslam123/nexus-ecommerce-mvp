package com.ecommerce.mvp.modules.order

import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ShipOrderRequestDto
import com.ecommerce.mvp.modules.order.service.OrderService
import jakarta.validation.Valid
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

}