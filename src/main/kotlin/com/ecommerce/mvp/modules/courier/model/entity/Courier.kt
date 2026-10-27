package com.ecommerce.mvp.modules.courier.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.Shipment
import com.ecommerce.mvp.common.entity.user.User
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "couriers")
class Courier : BaseEntityAudit() {

    @OneToOne
    @JoinColumn(name = "shipment_id", unique = true)
    var shipment: Shipment? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

}
