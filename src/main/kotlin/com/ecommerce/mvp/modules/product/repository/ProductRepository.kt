package com.ecommerce.mvp.modules.product.repository

import com.practice.ecommerce.ecommerce.modules.product.model.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByName(name: String): Optional<Product>
}
