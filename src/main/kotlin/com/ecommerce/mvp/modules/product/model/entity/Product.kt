package com.ecommerce.mvp.modules.product.model.entity

import com.ecommerce.mvp.common.entity.audit.BaseEntityAudit
import jakarta.persistence.*
import java.math.BigDecimal


@Entity
@Table(name = "products")
open class Product : BaseEntityAudit() {

    @Column(nullable = false)
    var name: String = ""
    
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO
    
    @Column(nullable = false)
    var stock: Int = 0
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_tags",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var images: MutableSet<ProductImage> = mutableSetOf()

}