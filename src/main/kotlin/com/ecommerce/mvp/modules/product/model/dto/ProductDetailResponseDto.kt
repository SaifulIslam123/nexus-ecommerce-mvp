package com.ecommerce.mvp.modules.product.model.dto

import com.ecommerce.mvp.modules.product.model.entity.Product
import java.math.BigDecimal
import java.util.Date

data class CategorySummaryDto(
    val id: Long?,
    val name: String?,
    val description: String?
)

data class ProductDetailResponseDto(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val inStock: Boolean,
    val description: String?,
    val category: CategorySummaryDto?,
    val tags: List<String>,
    val createdDate: Date?,
    val modifiedDate: Date?
)

fun Product.toDetailResponseDto(): ProductDetailResponseDto {
    return ProductDetailResponseDto(
        id = this.id!!,
        name = this.name,
        price = this.price,
        stock = this.stock,
        inStock = this.stock > 0,
        description = this.description,
        category = this.category?.let {
            CategorySummaryDto(
                id = it.id,
                name = it.name,
                description = it.description
            )
        },
        tags = this.tags.mapNotNull { it.name },
        createdDate = this.createdDate,
        modifiedDate = this.modifiedDate
    )
}

