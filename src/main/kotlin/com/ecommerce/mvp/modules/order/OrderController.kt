package com.ecommerce.mvp.modules.order
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
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
}
