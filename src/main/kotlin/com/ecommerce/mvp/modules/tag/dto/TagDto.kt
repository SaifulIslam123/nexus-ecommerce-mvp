package com.ecommerce.mvp.modules.tag.dto

import com.ecommerce.mvp.modules.product.model.entity.Tag
import jakarta.validation.constraints.NotBlank

data class TagRequestDto(
    @field:NotBlank(message = "Tag name is required")
    val name: String
)

data class TagResponseDto(
    val id: Long,
    val name: String
)

fun Tag.toResponseDto() = TagResponseDto(
    id = this.id ?: -1,
    name = this.name ?: ""
)

