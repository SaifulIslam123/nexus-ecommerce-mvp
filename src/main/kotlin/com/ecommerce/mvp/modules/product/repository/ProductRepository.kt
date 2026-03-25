package com.ecommerce.mvp.modules.product.repository

import com.ecommerce.mvp.modules.category.entity.Category
import com.ecommerce.mvp.modules.product.model.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

        /*@Query("SELECT p FROM Product u WHERE u.status = :status")
        fun findById(id: Long): List<Product>*/
        fun findByCategoryId(id: Long): List<Product>
        fun findAllByCategory(category: Category): List<Product>
}
