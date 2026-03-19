package com.ecommerce.mvp.modules.product.model.dto

import com.ecommerce.mvp.modules.product.model.entity.Product
import java.math.BigDecimal
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
    val categoryId: Long?,
    val categoryName: String?,
    val tags: List<String>/*,
    val images: List<ProductImageResponseDto>*/
)

fun Product.toResponseDto(): ProductResponseDto {
    return ProductResponseDto(
        id = this.id!!,
        name = this.name,
        price = this.price,
        stock = this.stock,
        description = this.description,
        categoryId = this.category?.id,
        categoryName = this.category?.name,
        tags = this.tags.mapNotNull { it.name }/*,
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