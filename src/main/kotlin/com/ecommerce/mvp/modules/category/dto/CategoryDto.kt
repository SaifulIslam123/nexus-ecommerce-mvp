package com.ecommerce.mvp.modules.category.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import com.ecommerce.mvp.modules.category.entity.Category

/**
 * Extension method to convert Category entity to CategoryDto
 */
fun Category.toDto(): CategoryDto {
    return CategoryDto(
        id = this.id,
        name = this.name ?: "",
        description = this.description
    )
}

/**
 * Extension method to convert CategoryDto to Category entity
 */
fun CategoryDto.toEntity(): Category {
    return Category().apply {
        name = this@toEntity.name
        description = this@toEntity.description
    }
}

/**
 * Extension method to update Category entity from CategoryDto
 */
fun Category.updateFrom(categoryDto: CategoryDto) {
    this.name = categoryDto.name
    this.description = categoryDto.description
}

data class CategoryDto(
    val id: Long? = null,

    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null
)


