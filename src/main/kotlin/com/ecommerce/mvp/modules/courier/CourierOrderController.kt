package com.ecommerce.mvp.modules.courier

import com.ecommerce.mvp.modules.courier.service.CourierService
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/courier/orders")
@PreAuthorize("hasRole('COURIER')")
class CourierOrderController(
    private val courierService: CourierService,
) {

    /**
     * GET /api/v1/courier/orders
     *
     * Paginated order list for the courier. Optional ?status= filter restricted to
     * SHIPPED, OUT_FOR_DELIVERY, or DELIVERED. If omitted, all three are returned.
     */
    @GetMapping
    fun getOrders(
        @RequestParam status: OrderStatus? = null,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): Page<OrderResponseDto> {
        return courierService.getOrdersForCourier(status, page, size)
    }

    /**
     * GET /api/v1/courier/orders/{id}
     *
     * Returns full details of any single order by ID.
     * Responds with 404 if the order does not exist.
     */
    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): OrderResponseDto {
        return courierService.getOrderByIdForCourier(id)
    }

    /**
     * PUT /api/v1/courier/orders/{id}/out-for-delivery
     *
     * Transitions a SHIPPED order to OUT_FOR_DELIVERY, signalling that the
     * package is with the local courier for final delivery.
     *
     * Allowed transition: SHIPPED → OUT_FOR_DELIVERY
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 if the order is not in SHIPPED status.
     */
    @PutMapping("/{id}/out-for-delivery")
    fun markAsOutForDelivery(@PathVariable id: Long): OrderResponseDto {
        return courierService.markAsOutForDelivery(id)
    }

    /**
     * PUT /api/v1/courier/orders/{id}/delivered
     *
     * Transitions an OUT_FOR_DELIVERY order to DELIVERED, signalling that the
     * package has been successfully received by the customer.
     *
     * Allowed transition: OUT_FOR_DELIVERY → DELIVERED
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 if the order is not in OUT_FOR_DELIVERY status.
     */
    @PutMapping("/{id}/delivered")
    fun markAsDelivered(@PathVariable id: Long): OrderResponseDto {
        return courierService.markAsDelivered(id)
    }
}
