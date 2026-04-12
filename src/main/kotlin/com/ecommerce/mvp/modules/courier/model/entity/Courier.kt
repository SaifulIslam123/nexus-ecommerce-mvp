package com.ecommerce.mvp.modules.courier.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.order.model.entity.Order
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "couriers")
class Courier : BaseEntityAudit() {

    @field:Column(nullable = false)
    var shipmentDate: Instant? = null

    @Column(name = "tracking_id", nullable = false, unique = true)
    var trackingId: String? = null

    @Column(name = "order_id", nullable = false)
    var orderId: Long? = null

    @Column(name = "shipment_address", nullable = false)
    var shipmentAddress: String? = null
}
