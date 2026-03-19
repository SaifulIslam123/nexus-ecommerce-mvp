package com.ecommerce.mvp.modules.product.service

import ProductResponseDto
import com.ecommerce.mvp.modules.product.model.dto.ProductSearchRequest
import com.ecommerce.mvp.modules.product.model.entity.Product
import com.ecommerce.mvp.modules.product.repository.ProductRepository
import com.ecommerce.mvp.modules.product.repository.ProductSpecification
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import toResponseDto
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository
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

    fun searchProducts(request: ProductSearchRequest): Page<ProductResponseDto> {
        val direction = if (request.direction.equals("asc", ignoreCase = true))
            Sort.Direction.ASC else Sort.Direction.DESC

        val pageable = PageRequest.of(request.page, request.size, Sort.by(direction, request.sort))
        val spec = ProductSpecification.build(request)

        return productRepository.findAll(spec, pageable)
            .map { it.toResponseDto()}
    }
}



