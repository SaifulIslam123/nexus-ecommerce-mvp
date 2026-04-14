package com.ecommerce.mvp.modules.order
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ToPayOrderRequest
import com.ecommerce.mvp.modules.order.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users/orders")
class OrderController(
    private val orderService: OrderService
) {
    /**
     * GET /api/orders
     *
     * Returns a list of all orders placed by the authenticated user,
     * sorted from newest to oldest.
     * Each entry includes the full order details: line items, payment,
     * and shipment information.
     */
    @GetMapping
    fun getMyOrders(): List<OrderResponseDto> {
        return orderService.getMyOrders()
    }

    /**
     * GET /api/orders/{id}
     *
     * Returns the full details of a single order belonging to the
     * authenticated user, including all line items, payment info,
     * and shipment info.
     *
     * Responds with 404 if the order does not exist or does not
     * belong to the current user.
     */
    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): OrderResponseDto {
        return orderService.getOrderById(id)
    }

    /**
     * PUT /api/orders/{id}/cancel
     *
     * Cancels an order that belongs to the authenticated user.
     * Only orders with status PENDING or PAID can be cancelled.
     * Restores the reserved stock for every line item in the order.
     *
     * Responds with 204 No Content on success.
     * Responds with 404 if the order does not exist or does not belong to
     * the current user, and 409 Conflict if the status does not allow
     * cancellation.
     */
    @PutMapping("/{id}/cancel")
    fun cancelOrder(@PathVariable id: Long): OrderResponseDto {
        return orderService.cancelOrder(id)
    }

    @PostMapping("/toPayOrder")
    fun toPayOrder(
        @Valid @RequestBody toPayOrderRequest:ToPayOrderRequest
    ): OrderResponseDto {
        return orderService.toPayOrder( toPayOrderRequest)
    }

    @DeleteMapping
    fun deleteOrders(@RequestBody requestDto: Long): ResponseEntity<Unit> {
        orderService.deleteById(requestDto)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PutMapping("/{id}/received")
    fun markAsReceived(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsReceived(id)
    }

    /**
     * PUT /api/users/orders/{id}/return
     *
     * Allows the authenticated user to mark an order as RETURNED.
     * The order must already be in RECEIVED status; otherwise a 409 Conflict
     * is returned.
     *
     * Allowed transition:  RECEIVED → RETURNED
     */
    @PutMapping("/{id}/return")
    fun markAsReturned(@PathVariable id: Long): OrderResponseDto {
        return orderService.markAsReturned(id)
    }

}
