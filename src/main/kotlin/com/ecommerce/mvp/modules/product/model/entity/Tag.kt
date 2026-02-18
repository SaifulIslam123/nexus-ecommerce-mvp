package com.ecommerce.mvp.modules.product.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*


@Entity
@Table(name = "tags")
class Tag : BaseEntityAudit() {

    @Column(nullable = false, unique = true)
    var name: String? = null

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    var products: MutableSet<Product> = mutableSetOf()

}

