package com.ecommerce.mvp.modules.product.model.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * Request DTO for creating a new product.
 * All required fields must be provided.
 */
data class ProductCreateRequestDto(

    @field:NotBlank(message = "Product name is required")
    val name: String,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: BigDecimal,

    @field:Min(value = 0, message = "Stock cannot be negative")
    val stock: Int = 0,

    val description: String? = null,

    @field:NotNull(message = "Category ID is required")
    val categoryId: Long,

    @field:NotNull(message = "Tag ID is required")
    val tags: List<Long> = emptyList(),

    val isActive: Boolean = true
)

/**
 * Request DTO for updating an existing product.
 * All fields are optional — only non-null fields are applied.
 */
data class ProductUpdateRequestDto(
    val name: String? = null,

    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: BigDecimal? = null,

    @field:Min(value = 0, message = "Stock cannot be negative")
    val stock: Int? = null,

    val description: String? = null,

    val categoryId: Long? = null,

    /** When provided, replaces the full tag list. */
    val tags: List<Long>? = null,

    val isActive: Boolean? = null
)

