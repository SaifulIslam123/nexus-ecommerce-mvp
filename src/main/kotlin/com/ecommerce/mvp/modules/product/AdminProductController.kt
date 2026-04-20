package com.ecommerce.mvp.modules.product

import com.ecommerce.mvp.modules.product.model.dto.ProductCreateRequestDto
import com.ecommerce.mvp.modules.product.model.dto.ProductResponseDto
import com.ecommerce.mvp.modules.product.model.dto.ProductUpdateRequestDto
import com.ecommerce.mvp.modules.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/products")
class AdminProductController(
    private val productService: ProductService
) {

    /**
     * POST /api/admin/products
     *
     * Creates a new product. The category must already exist.
     * Tags are created automatically if they do not exist yet.
     *
     * Responds with 201 Created on success.
     * Responds with 400 if validation fails.
     * Responds with 404 if the given category ID does not exist.
     */
    @PostMapping
    fun createProduct(
        @Valid @RequestBody requestDto: ProductCreateRequestDto
    ): ResponseEntity<ProductResponseDto> {
        val created = productService.createProduct(requestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /**
     * PUT /api/admin/products/{id}
     *
     * Updates an existing product. Only fields provided in the request body
     * are changed — omitted fields keep their current values (partial update).
     * When `tags` is included the full tag list is replaced.
     *
     * Responds with 404 if the product or category does not exist.
     */
    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody requestDto: ProductUpdateRequestDto
    ): ProductResponseDto {
        return productService.updateProduct(id, requestDto)
    }

    /**
     * PATCH /api/admin/products/{id}/toggle-active
     *
     * Flips the `isActive` flag of the product.
     * Active → Inactive, Inactive → Active.
     * Useful for temporarily hiding a product without deleting it.
     *
     * Responds with 404 if the product does not exist.
     */
    @PatchMapping("/{id}/toggle-active")
    fun toggleProductActive(@PathVariable id: Long): ProductResponseDto {
        return productService.toggleProductActive(id)
    }

    /**
     * DELETE /api/admin/products/{id}
     *
     * Permanently removes a product.
     * Prefer using `toggle-active` to hide a product instead of deleting it,
     * unless you are sure the product must be fully removed.
     *
     * Responds with 204 No Content on success.
     * Responds with 404 if the product does not exist.
     */
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Unit> {
        productService.deleteProduct(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}

