package com.ecommerce.mvp.modules.product.service

import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.category.repository.CategoryRepository
import com.ecommerce.mvp.modules.product.model.dto.ProductCreateRequestDto
import com.ecommerce.mvp.modules.product.model.dto.ProductResponseDto
import com.ecommerce.mvp.modules.product.model.dto.ProductSearchRequest
import com.ecommerce.mvp.modules.product.model.dto.ProductUpdateRequestDto
import com.ecommerce.mvp.modules.product.model.dto.toResponseDto
import com.ecommerce.mvp.modules.product.model.entity.Product
import com.ecommerce.mvp.modules.product.model.entity.Tag
import com.ecommerce.mvp.modules.product.repository.ProductRepository
import com.ecommerce.mvp.modules.product.repository.ProductSpecification
import com.ecommerce.mvp.modules.product.repository.TagRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository
) {

    @Transactional
    fun insertSampleProduct() {
        val newProduct = Product().apply {
            price = BigDecimal(19.99)
            name = "Sample Product"
            description = "This is a sample product."
        }
        productRepository.save(newProduct)
    }

    fun getProductById(id: Long): ProductResponseDto {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        return product.toResponseDto()
    }

    fun getProductDetail(id: Long): ProductResponseDto {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        return product.toResponseDto()
    }

    @Transactional
    fun searchProducts(request: ProductSearchRequest): Page<ProductResponseDto> {
        val direction = if (request.direction.equals("asc", ignoreCase = true))
            Sort.Direction.ASC else Sort.Direction.DESC

        val pageable = PageRequest.of(request.page, request.size, Sort.by(direction, request.sort))
        val spec = ProductSpecification.build(request)

        return productRepository.findAll(spec, pageable)
            .map { it.toResponseDto() }
    }

    @Transactional
    fun getRecommendedProduct(id: Long): List<ProductResponseDto> {

        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        product.category.id?.let {
            val productList = productRepository.findAllByCategory(product.category)

            return productList.map { it.toResponseDto() }

        }.run {
            throw ResourceNotFoundException("Product category not found with id: $id")
        }
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    /**
     * Admin: Creates a brand-new product.
     * Tags are looked up by name and created on the fly if they do not yet exist.
     */
    @Transactional
    fun createProduct(requestDto: ProductCreateRequestDto): ProductResponseDto {

        val category = categoryRepository.findById(requestDto.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found with id: ${requestDto.categoryId}") }

        val product = Product().apply {
            name = requestDto.name
            price = requestDto.price
            stock = requestDto.stock
            description = requestDto.description
            isActive = requestDto.isActive
            this.category = category
        }

        requestDto.tags.forEach { id -> product.tags.add(tagRepository.getReferenceById(id)) }

        return productRepository.save(product).let { it.toResponseDto() }
    }

    /**
     * Admin: Partially updates an existing product.
     * Only non-null fields in the request are applied.
     * When [ProductUpdateRequestDto.tags] is provided the entire tag list is replaced.
     */
    @Transactional
    fun updateProduct(id: Long, requestDto: ProductUpdateRequestDto): ProductResponseDto {

        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }

        requestDto.name?.let { product.name = it }
        requestDto.price?.let { product.price = it }
        requestDto.stock?.let { product.stock = it }
        requestDto.description?.let { product.description = it }
        requestDto.isActive?.let { product.isActive = it }

        requestDto.categoryId?.let { categoryId ->
            val category = categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found with id: $categoryId") }
            product.category = category
        }

        requestDto.tags?.let { tagIds ->
            product.tags.clear()
            tagIds.forEach { id -> product.tags.add(tagRepository.getReferenceById(id)) }
        }

        return product.toResponseDto()
    }

    /**
     * Admin: Permanently deletes a product by ID.
     * Throws [ResourceNotFoundException] if the product does not exist.
     */
    @Transactional
    fun deleteProduct(id: Long) {
        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException("Product not found with id: $id")
        }
        productRepository.deleteById(id)
    }

    /**
     * Admin: Toggles the [Product.isActive] flag.
     * Active → Inactive, Inactive → Active.
     */
    @Transactional
    fun toggleProductActive(id: Long): ProductResponseDto {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        product.isActive = !product.isActive
        return product.toResponseDto()
    }
}

