package com.ecommerce.mvp.modules.courier.repository

import com.ecommerce.mvp.modules.courier.model.entity.Courier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CourierRepository : JpaRepository<Courier, Long> {

    fun findByOrderId(orderId: Long): Optional<Courier>

    fun existsByOrderId(orderId: Long): Boolean

    fun existsByTrackingId(trackingId: String): Boolean
}
