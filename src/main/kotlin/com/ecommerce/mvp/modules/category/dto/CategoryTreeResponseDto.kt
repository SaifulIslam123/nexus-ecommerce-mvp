package com.ecommerce.mvp.modules.category.dto

import com.ecommerce.mvp.modules.category.entity.Category
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CategoryTreeResponseDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val parentId: Long?,
    val children: List<CategoryTreeResponseDto> = emptyList()
)

fun Category.toTreeDto(): CategoryTreeResponseDto {
    return CategoryTreeResponseDto(
        id = this.id,
        name = this.name,
        description = this.description,
        parentId = this.parent?.id,
        children = this.children.map { it.toTreeDto() }
    )
}

