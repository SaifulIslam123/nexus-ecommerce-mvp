package com.ecommerce.mvp.modules.courier.model.dto

import com.ecommerce.mvp.modules.courier.model.entity.Courier
import com.ecommerce.mvp.modules.order.model.dto.ShipmentResponseDto
import com.ecommerce.mvp.modules.order.model.dto.toResponseDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

// ── Request ───────────────────────────────────────────────────────────────────

data class CourierRequestDto(
    @field:NotNull(message = "ShipmentDate is required")
    val shipmentDate: LocalDate,
    @field:NotBlank(message = "ShipmentAddress is required")
    val shipmentAddress: String,
    @field:NotNull(message = "OrderId is required")
    val orderId: Long,
)

// ── Response ──────────────────────────────────────────────────────────────────

data class CourierResponseDto(
    val id: Long,
    val shipment: ShipmentResponseDto?,
)

fun Courier.toResponseDto() = CourierResponseDto(
    id = this.id ?: -1,
    shipment = this.shipment?.toResponseDto()
)
