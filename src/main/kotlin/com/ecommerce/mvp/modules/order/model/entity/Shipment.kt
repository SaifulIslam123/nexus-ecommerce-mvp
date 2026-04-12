package com.ecommerce.mvp.modules.order.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.user.model.entity.Address
import jakarta.persistence.*
import java.time.Instant


@Entity
@Table(name = "shipments")
class Shipment : BaseEntityAudit() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_address_id")
    var shipmentAddress: Address? = null

    @field:Column(nullable = false)
    var estimatedDeliveryDate: Instant? = null

    @Column(nullable = false)
    var status: String? = null

    @Column(unique = true)
    var trackingId: String? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    var order: Order? = null

}

