package com.ecommerce.mvp.modules.product.service

import com.practice.ecommerce.ecommerce.modules.product.model.entity.Product
import com.practice.ecommerce.ecommerce.modules.product.repository.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Transactional
    fun insertSampleProduct() {

        val newProduct = Product().apply {
            price = BigDecimal(19.99)
            name = "Sample Product"
            description = "This is a sample product."
        }

        productRepository.save(newProduct)
    }

}