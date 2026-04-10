package com.ecommerce.mvp.modules.product.model.dto

import com.ecommerce.mvp.modules.product.model.entity.Product
import java.math.BigDecimal
import java.time.Instant

data class CategorySummaryDto(
    val id: Long?,
    val name: String?,
    val description: String?
)

data class ProductImageResponseDto(
    val id: Long,
    val imageUrl: String?,
    val altText: String?,
    val isPrimary: Boolean
)
data class ProductResponseDto(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val description: String?,
    val category: CategorySummaryDto?,
    val createdDate: Instant?,
    val modifiedDate: Instant?,
    val tags: List<String>/*,
    val images: List<ProductImageResponseDto>*/
)

fun Product.toResponseDto(): ProductResponseDto {
    return ProductResponseDto(
        id = this.id ?: -1,
        name = this.name,
        price = this.price,
        stock = this.stock,
        description = this.description,
        category = this.category.let {
            CategorySummaryDto(
                id = it.id,
                name = it.name,
                description = it.description
            )
        },
        tags = this.tags.mapNotNull { it.name },
        createdDate = this.createdDate,
        modifiedDate = this.modifiedDate/*,
        images = this.images.map { img ->
            ProductImageResponseDto(
                id = img.id!!,
                imageUrl = img.imageUrl,
                altText = img.altText,
                isPrimary = img.isPrimary
            )
        }*/
    )
}