package com.ecommerce.mvp.modules.product

import com.ecommerce.mvp.modules.product.model.dto.ProductResponseDto
import com.ecommerce.mvp.common.response.ApiResponse
import com.ecommerce.mvp.modules.product.model.dto.ProductSearchRequest
import com.ecommerce.mvp.modules.product.service.ProductService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService
) {

    /** Known query parameter names — these are NOT treated as dynamic attributes. */
    private val reservedParams = setOf(
        "keyword", "categoryIds", "minPrice", "maxPrice", "tags", "page", "size", "sort", "direction"
    )

    /**
     * GET /api/products
     *
     * Supports the following query parameters:
     *  - keyword       : free-text search in name / description
     *  - categoryIds   : comma-separated list of category IDs  (e.g. ?categoryIds=1,2,3)
     *  - minPrice      : minimum price (inclusive)
     *  - maxPrice      : maximum price (inclusive)
     *  - tags          : comma-separated tag names             (e.g. ?tags=summer,sale)
     *  - page          : 0-based page index (default 0)
     *  - size          : page size (default 20)
     *  - sort          : field to sort by (default createdDate)
     *  - direction     : asc | desc (default desc)
     *  - <anything else>: treated as a dynamic attribute filter (e.g. ?color=red&size=M)
     */
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ProductResponseDto {
        val product = productService.getProductById(id)
        return product
    }

    @GetMapping
    fun searchProducts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) categoryIds: List<Long>?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdDate") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        @RequestParam allParams: Map<String, String>
    ): Page<ProductResponseDto> {

        // Separate dynamic attribute params from the known/reserved ones
        val attributes = allParams.filterKeys { it !in reservedParams }

        val request = ProductSearchRequest(
            keyword = keyword,
            categoryIds = categoryIds,
            minPrice = minPrice,
            maxPrice = maxPrice,
            tags = tags,
            dynamicAttributes = attributes,
            page = page,
            size = size,
            sort = sort,
            direction = direction
        )

        return productService.searchProducts(request)

    }

    @GetMapping("rec/{id}")
    fun getRecommendedProductById(@PathVariable id: Long): List<ProductResponseDto> {
        return productService.getRecommendedProduct(id)
    }
}

