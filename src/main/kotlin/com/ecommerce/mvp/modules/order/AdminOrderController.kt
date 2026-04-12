package com.ecommerce.mvp.modules.order

import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.service.OrderService
import org.springframework.web.bind.annotation.*
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
        @PathVariable id: Long
    ): OrderResponseDto {
        return orderService.markAsShipped(id)
    }


    @GetMapping("/byStatus")
    fun getOrdersByStatus(@RequestParam status: OrderStatus): List<OrderResponseDto> {
        return orderService.getOrderByStatus(status)
    }

}