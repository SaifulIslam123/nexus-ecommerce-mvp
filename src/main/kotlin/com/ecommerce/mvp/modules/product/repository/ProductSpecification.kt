package com.ecommerce.mvp.modules.product.repository

import com.ecommerce.mvp.modules.category.entity.Category
import com.ecommerce.mvp.modules.product.model.dto.ProductSearchRequest
import com.ecommerce.mvp.modules.product.model.entity.Product
import com.ecommerce.mvp.modules.product.model.entity.Tag
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification

object ProductSpecification {

    fun build(request: ProductSearchRequest): Specification<Product> {
        return Specification { root, query, cb ->

            // Use DISTINCT to avoid duplicate rows caused by joins
            query?.distinct(true)

            val predicates = mutableListOf<Predicate>()

            // 1. Keyword search — matches name or description (case-insensitive)
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                val pattern = "%${kw.lowercase()}%"
                val nameLike = cb.like(cb.lower(root.get("name")), pattern)
                val descLike = cb.like(cb.lower(root.get("description")), pattern)
                predicates.add(cb.or(nameLike, descLike))
            }

            // 2. Category IDs filter
            request.categoryIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                val categoryJoin: Join<Product, Category> = root.join("category", JoinType.LEFT)
                predicates.add(categoryJoin.get<Long>("id").`in`(ids))
            }

            // 3. Price range filter
            request.minPrice?.let { min ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min))
            }
            request.maxPrice?.let { max ->
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), max))
            }

            // 4. Tags filter — product must have ALL requested tags
            request.tags?.takeIf { it.isNotEmpty() }?.forEach { tagName ->
                val tagSubquery: Subquery<Long> = query!!.subquery(Long::class.java)
                val tagRoot: Root<Tag> = tagSubquery.from(Tag::class.java)
                tagSubquery.select(tagRoot.get("id"))

                val productTagJoin: Join<Tag, Product> = tagRoot.join("products")
                tagSubquery.where(
                    cb.and(
                        cb.equal(productTagJoin.get<Long>("id"), root.get<Long>("id")),
                        cb.equal(cb.lower(tagRoot.get("name")), tagName.lowercase())
                    )
                )
                predicates.add(cb.exists(tagSubquery))
            }

            // 5. Dynamic attribute filters
            //    The current schema has no EAV / attribute table, so each attribute value
            //    is matched as a keyword against name + description.
            //    Replace this block with a proper attribute-table join once the schema grows.
            request.attributes.forEach { (_, value) ->
                if (value.isNotBlank()) {
                    val pattern = "%${value.lowercase()}%"
                    val nameLike = cb.like(cb.lower(root.get("name")), pattern)
                    val descLike = cb.like(cb.lower(root.get("description")), pattern)
                    predicates.add(cb.or(nameLike, descLike))
                }
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
