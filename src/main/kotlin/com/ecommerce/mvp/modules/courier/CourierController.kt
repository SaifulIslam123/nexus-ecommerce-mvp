package com.ecommerce.mvp.modules.courier

import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.courier.model.dto.CourierRequestDto
import com.ecommerce.mvp.modules.courier.model.dto.CourierResponseDto
import com.ecommerce.mvp.modules.courier.service.CourierService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/couriers")
class CourierController(
    private val courierService: CourierService
) {

    /**
     * POST /api/admin/couriers
     *
     * Creates a new courier record that links a tracking ID to an existing order.
     * Responds with 201 Created on success.
     */
    @PostMapping
    fun createCourier(
        @Valid @RequestBody requestDto: CourierRequestDto
    ): CourierResponseDto {
        return courierService.createCourier(requestDto)
    }

    /**
     * GET /api/admin/couriers
     *
     * Returns all courier records.
     */
    @GetMapping
    fun getAllCouriers(): ApiResponse<List<CourierResponseDto>> {
        return ApiResponse(
            success = true,
            message = "Couriers fetched successfully",
            data = courierService.getAllCouriers()
        )
    }

    /**
     * GET /api/admin/couriers/{id}
     *
     * Returns a single courier by its own ID.
     * Responds with 404 if no courier with that ID exists.
     */
    @GetMapping("/{id}")
    fun getCourierById(@PathVariable id: Long): ApiResponse<CourierResponseDto> {
        return ApiResponse(
            success = true,
            message = "Courier fetched successfully",
            data = courierService.getCourierById(id)
        )
    }

    /**
     * GET /api/admin/couriers/order/{orderId}
     *
     * Returns the courier record associated with the given order.
     * Responds with 404 if the order has no courier record yet.
     */
    @GetMapping("/order/{orderId}")
    fun getCourierByOrderId(@PathVariable orderId: Long): ApiResponse<CourierResponseDto> {
        return ApiResponse(
            success = true,
            message = "Courier fetched successfully",
            data = courierService.getCourierByOrderId(orderId)
        )
    }

    /**
     * PUT /api/admin/couriers/{id}/tracking
     *
     * Updates the tracking ID of an existing courier.
     * Responds with 404 if the courier does not exist.
     * Responds with 400 if the tracking ID is already used by another courier.
     */
    @PutMapping("/{id}/tracking")
    fun updateTrackingId(
        @PathVariable id: Long,
        @RequestParam trackingId: String
    ): ApiResponse<CourierResponseDto> {
        return ApiResponse(
            success = true,
            message = "Tracking ID updated successfully",
            data = courierService.updateTrackingId(id, trackingId)
        )
    }

    /**
     * DELETE /api/admin/couriers/{id}
     *
     * Deletes a courier record by its ID.
     * Responds with 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    fun deleteCourier(@PathVariable id: Long): ResponseEntity<Void> {
        courierService.deleteCourier(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
