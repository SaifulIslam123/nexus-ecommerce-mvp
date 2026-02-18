package com.ecommerce.mvp.modules.product.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*


@Entity
@Table(name = "categories")
class Category : BaseEntityAudit() {

    @Column(nullable = false)
    var name: String? = null

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var products: MutableSet<Product> = mutableSetOf()

}

