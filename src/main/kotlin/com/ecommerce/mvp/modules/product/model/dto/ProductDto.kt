package com.ecommerce.mvp.modules.product.model.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ProductDto(
    val id: Long = 0,

    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255)
    val name: String,

    @field:NotNull
    @field:DecimalMin("0.01")
    val price: BigDecimal,

    @field:NotNull
    val stock: Int,

    val description: String? = null
)