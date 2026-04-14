package com.ecommerce.mvp.modules.courier

import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.courier.model.dto.CourierRequestDto
import com.ecommerce.mvp.modules.courier.model.dto.CourierResponseDto
import com.ecommerce.mvp.modules.courier.service.CourierService
import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.order.service.OrderService
import jakarta.validation.Valid
import lombok.experimental.PackagePrivate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/courier")
class CourierController(
    private val courierService: CourierService
) {
    /**
     * PUT /api/courier/orders/{id}/out-for-delivery
     *
     * courier operation. Transitions a SHIPPED order to OUT_FOR_DELIVERY,
     * signalling that the package is with the local courier for final delivery.
     *
     * Allowed transition:  SHIPPED → OUT_FOR_DELIVERY
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 Conflict if the order is not in SHIPPED status.
     */
    @PutMapping("/orders/{id}/out-for-delivery")
    fun markAsOutForDelivery(@PathVariable id: Long): OrderResponseDto {
        return courierService.markAsOutForDelivery(id)
    }

    /**
     * PUT /api/admin/orders/{id}/delivered
     *
     * courier operation. Transitions an OUT_FOR_DELIVERY order to DELIVERED,
     * signalling that the package has been successfully received by the customer.
     *
     * Allowed transition:  OUT_FOR_DELIVERY → DELIVERED
     *
     * Responds with 404 if the order does not exist.
     * Responds with 409 Conflict if the order is not in OUT_FOR_DELIVERY status.
     */
    @PutMapping("/orders/{id}/delivered")
    fun markAsDelivered(@PathVariable id: Long): OrderResponseDto {
        return courierService.markAsDelivered(id)
    }

    @GetMapping("/orders/byStatus")
    fun getOrdersByStatus(@RequestParam status: OrderStatus): List<OrderResponseDto> {
        return courierService.getOrderByStatus(status)
    }
}
