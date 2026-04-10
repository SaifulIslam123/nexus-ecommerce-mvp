package com.ecommerce.mvp.modules.order
import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ShipOrderRequestDto
import com.ecommerce.mvp.modules.order.service.OrderService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

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

    @PostMapping("/toPayOrder/{cartItemId}")
    fun toPayOrder(@PathVariable cartItemId: Long, @RequestBody addressId: Long?): OrderResponseDto {
        return orderService.toPayOrder(cartItemId, addressId)
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

    @GetMapping("/getTodayOrders")
    fun getTodayOrders(): List<OrderResponseDto> {
        return orderService.getDateOrder()
    }

    @DeleteMapping
    fun deleteOrders(@RequestBody requestDto: Long): ApiResponse<String> {
        orderService.deleteById(requestDto)
        return ApiResponse(success = true, message = "Done",data = "Order with id $requestDto has been deleted successfully.")
    }

    // GET /orders/by-date?date=2026-04-10
    @GetMapping("/by-date")
    fun getOrdersByDate(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate? = null
    ): List<OrderResponseDto> {
        return orderService.getOrdersByDate(startDate, endDate)
    }
}
