package com.ecommerce.mvp.modules.product.service

import com.practice.ecommerce.ecommerce.modules.product.model.dto.ProductDto
import com.practice.ecommerce.ecommerce.modules.product.model.entity.Product

object ProductMapper {
    fun toEntity(productDto: ProductDto): Product {
        return Product().apply {
            name = productDto.name
            price = productDto.price
            stock = productDto.stock
            description = productDto.description
        }
    }

    fun toResponse(product: Product): ProductDto {
        return ProductDto(
            id = product.id!!,
            name = product.name,
            price = product.price,
            stock = product.stock,
            description = product.description
        )
    }
}