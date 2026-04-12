package com.ecommerce.mvp.modules.courier.service

import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.courier.TrackingIdGenerator
import com.ecommerce.mvp.modules.courier.model.dto.CourierRequestDto
import com.ecommerce.mvp.modules.courier.model.dto.CourierResponseDto
import com.ecommerce.mvp.modules.courier.model.dto.toResponseDto
import com.ecommerce.mvp.modules.courier.model.entity.Courier
import com.ecommerce.mvp.modules.courier.repository.CourierRepository
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset

@Service
class CourierService(
    private val courierRepository: CourierRepository,
) {

    /**
     * Creates a new [Courier] record that links a tracking ID to an order.
     *
     * Validates that:
     * - the order actually exists
     * - the order does not already have a courier record attached
     * - the tracking ID is not already used by another courier
     *
     * Throws [ResourceNotFoundException] if the order is not found.
     * Throws [BusinessValidationException] if the order already has a courier
     * or the tracking ID is already in use.
     */
    @Transactional
    fun createCourier(requestDto: CourierRequestDto): CourierResponseDto {

        if (courierRepository.existsByOrderId(requestDto.orderId)) {
            throw BusinessValidationException("A courier record already exists for order id: ${requestDto.orderId}")
        }

        val courier = Courier().apply {
            shipmentDate = requestDto.shipmentDate.atStartOfDay(ZoneOffset.UTC).toInstant()
            shipmentAddress = requestDto.shipmentAddress
            trackingId = TrackingIdGenerator.generateTrackingId()
            orderId = requestDto.orderId
        }

        val savedCourier = courierRepository.save(courier)
        return savedCourier.toResponseDto()
    }

    /**
     * Returns the [Courier] record for the given [courierId].
     *
     * Throws [ResourceNotFoundException] if no record is found.
     */
    @Transactional(readOnly = true)
    fun getCourierById(courierId: Long): CourierResponseDto {
        val courier = courierRepository.findById(courierId)
            .orElseThrow { ResourceNotFoundException("Courier not found with id: $courierId") }
        return courier.toResponseDto()
    }

    /**
     * Returns the [Courier] record associated with the given [orderId].
     *
     * Throws [ResourceNotFoundException] if no record is found.
     */
    @Transactional(readOnly = true)
    fun getCourierByOrderId(orderId: Long): CourierResponseDto {
        val courier = courierRepository.findByOrderId(orderId)
            .orElseThrow { ResourceNotFoundException("Courier not found for order id: $orderId") }
        return courier.toResponseDto()
    }

    /**
     * Returns all courier records.
     */
    @Transactional(readOnly = true)
    fun getAllCouriers(): List<CourierResponseDto> {
        return courierRepository.findAll().map { it.toResponseDto() }
    }

    /**
     * Updates the tracking ID of an existing [Courier].
     *
     * Throws [ResourceNotFoundException] if the courier is not found.
     * Throws [BusinessValidationException] if the new tracking ID is already in use by another courier.
     */
    @Transactional
    fun updateTrackingId(courierId: Long, newTrackingId: String): CourierResponseDto {

        val courier = courierRepository.findById(courierId)
            .orElseThrow { ResourceNotFoundException("Courier not found with id: $courierId") }

        if (courier.trackingId != newTrackingId && courierRepository.existsByTrackingId(newTrackingId)) {
            throw BusinessValidationException("Tracking ID '$newTrackingId' is already in use")
        }

        courier.trackingId = newTrackingId
        return courier.toResponseDto()
    }

    /**
     * Deletes a courier record by [courierId].
     *
     * Throws [ResourceNotFoundException] if no record is found.
     */
    @Transactional
    fun deleteCourier(courierId: Long) {
        if (!courierRepository.existsById(courierId)) {
            throw ResourceNotFoundException("Courier not found with id: $courierId")
        }
        courierRepository.deleteById(courierId)
    }
}
