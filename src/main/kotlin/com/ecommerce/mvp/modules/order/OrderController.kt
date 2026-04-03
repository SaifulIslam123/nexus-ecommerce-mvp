package com.ecommerce.mvp.modules.order
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api/orders")
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
    fun toPayOrder(@RequestBody cartItemId: Long): OrderResponseDto {
        return orderService.toPayOrder(cartItemId)
    }
}
