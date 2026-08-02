package com.ecommerce.mvp.modules.product.repository

import com.ecommerce.mvp.modules.category.entity.Category
import com.ecommerce.mvp.modules.product.model.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {


        fun findAllByCategory(category: Category): List<Product>

        /** Dashboard: products whose stock is at or below the given threshold. */
        @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.isActive = true ORDER BY p.stock ASC")
        fun findLowStockProducts(@org.springframework.data.repository.query.Param("threshold") threshold: Int): List<Product>

        /** Dashboard: total count of active products. */
        fun countByIsActiveTrue(): Long

        fun findByIdAndIsActiveTrue(productId: Long): Optional<Product>

}
