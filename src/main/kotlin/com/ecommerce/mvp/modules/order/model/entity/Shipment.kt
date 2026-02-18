package com.ecommerce.mvp.modules.order.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*


@Entity
@Table(name = "shipments")
class Shipment : BaseEntityAudit() {

    @Column(nullable = false, columnDefinition = "TEXT")
    var address: String? = null

    @Column(nullable = false)
    var status: String? = null

    @Column(unique = true)
    var trackingId: String? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    var order: Order? = null

}

