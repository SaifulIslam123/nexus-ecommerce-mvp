package com.ecommerce.mvp.modules.category.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import com.ecommerce.mvp.modules.product.model.entity.Product
import jakarta.persistence.*


@Entity
@Table(name = "categories")
class Category : BaseEntityAudit() {

    @Column(nullable = false)
    var name: String? = null

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = false, fetch = FetchType.LAZY)
    var children: MutableList<Category> = mutableListOf()

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var products: MutableSet<Product> = mutableSetOf()

}

