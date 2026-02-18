package com.ecommerce.mvp.modules.product.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*


@Entity
@Table(name = "product_images")
class ProductImage : BaseEntityAudit() {

    @Column(nullable = false)
    var imageUrl: String? = null

    @Column
    var altText: String? = null

    @Column(nullable = false)
    var isPrimary: Boolean = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product? = null

}

