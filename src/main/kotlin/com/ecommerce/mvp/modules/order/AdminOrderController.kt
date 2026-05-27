package com.ecommerce.mvp.modules.order

import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.UpdateOrderStatusRequest
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.service.OrderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/admin/orders")
class AdminOrderController(
    private val orderService: OrderService
) {

    /**
     * GET /api/admin/orders
     *
     * Returns admin created orders in the system, sorted newest first.
     * Admin-only endpoint — no user filter is applied.
     */
    @GetMapping("/my")
    fun getAllMyOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<OrderResponseDto> {
        return orderService.getMyOrders(page, size)
    }

    /**
     * GET /api/admin/orders
     *
     * Returns every order in the system, sorted newest first.
     * Admin-only endpoint — no user filter is applied.
     */
    @GetMapping
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<OrderResponseDto> {
        return orderService.getAdminAllOrders(page, size)
    }

    /**
     * GET /api/admin/orders/{id}
     *
     * Returns the full details of any single order by ID.
     * Admin-only — not restricted to the authenticated user's orders.
     *
     * Responds with 404 if the order does not exist.
     */
    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): OrderResponseDto {
        return orderService.getOrderById(id)
    }

    // Expected date format 2026-04-10
    @GetMapping("/byDate")
    fun getOrdersByDate(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate? = null
    ): List<OrderResponseDto> {
        return orderService.getOrdersByDate(startDate, endDate)
    }

    @GetMapping("/byStatus")
    fun getOrdersByStatus(@RequestParam status: OrderStatus): List<OrderResponseDto> {
        return orderService.getOrderByStatus(status)
    }

    /**
     * PATCH /api/admin/orders/{id}/status
     *
     * General-purpose status updater for admins.
     * Validates the transition is legal, applies any side-effects
     * (stock restore, tracking-id generation, etc.) and sends an
     * email notification to the customer.
     *
     * Request body: { "status": "SHIPPED" }
     */
    @PatchMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): OrderResponseDto {
        return orderService.updateOrderStatus(id, request.status)
    }

    /**
     * PUT /api/admin/orders/{id}/processing
     *
     * Transitions a CONFIRMED order to PROCESSING,
     * signalling that the warehouse has started preparing the shipment.
     *
     * Allowed transition:  CONFIRMED → PROCESSING
     */
    @PutMapping("/{id}/processing")
    fun markAsProcessing(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsProcessing(id)
    }

    /**
     * PUT /api/admin/orders/{id}/shipped
     *
     * Transitions a PROCESSING order to SHIPPED
     * and assigns the carrier tracking number.
     *
     * Allowed transition:  PROCESSING → SHIPPED
     */
    @PutMapping("/{id}/shipped")
    fun markAsShipped(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsShipped(id)
    }

    /**
     * PUT /api/admin/orders/{id}/refund
     *
     * Transitions a RETURNED order to REFUNDED and restores stock.
     *
     * Allowed transition:  RETURNED → REFUNDED
     */
    @PutMapping("/{id}/refund")
    fun markAsRefunded(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsRefunded(id)
    }

    /**
     * DELETE /api/admin/orders/{id}
     *
     * Permanently removes an order record.
     * Use with caution — prefer cancellation over deletion in production.
     */
    @DeleteMapping("/{id}")
    fun deleteOrder(@PathVariable id: Long): ResponseEntity<Unit> {
        orderService.deleteById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}