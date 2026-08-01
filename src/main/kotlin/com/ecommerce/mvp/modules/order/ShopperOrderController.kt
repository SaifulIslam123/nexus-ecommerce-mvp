package com.ecommerce.mvp.modules.order

import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.dto.ToPayOrderRequest
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.service.OrderService
import com.ecommerce.mvp.modules.order.service.ShopperOrderService
import com.ecommerce.mvp.security.IsShopper
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/shopper/orders")
@IsShopper
class ShopperOrderController {

    private final val orderService: ShopperOrderService

    @Autowired
    constructor(@Qualifier("shopperOrderService") shopperOrderService: ShopperOrderService) {
        this.orderService = shopperOrderService
    }

    /**
     * GET /api/orders
     *
     * Returns a list of all orders placed by the authenticated user,
     * sorted from newest to oldest.
     * Each entry includes the full order details: line items, payment,
     * and shipment information.
     */
    @GetMapping
    fun getAllMyOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<OrderResponseDto> {
        return orderService.getMyOrders(page, size)
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
    fun getOrderById(@PathVariable id: Long): OrderResponseDto {
        return orderService.getUserOrderById(id)
    }


    @PostMapping("/toPayOrder")
    fun toPayOrder(
        @Valid @RequestBody toPayOrderRequest: ToPayOrderRequest
    ): OrderResponseDto {
        return orderService.toPayOrder(toPayOrderRequest)
    }

    @PutMapping("/{id}/cancel")
    fun cancelOrder(@PathVariable id: Long): OrderResponseDto {
        return orderService.shopperUpdateOrderStatus(id, OrderStatus.CANCELLED)
    }

    @PutMapping("/{id}/received")
    fun markAsReceived(@PathVariable id: Long): OrderResponseDto {
        return orderService.shopperUpdateOrderStatus(id, OrderStatus.RECEIVED)
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
        return orderService.shopperUpdateOrderStatus(id, OrderStatus.RETURNED)
    }

}
