package com.ecommerce.mvp.modules.product.model.dto

import java.math.BigDecimal

/**
 * Encapsulates all supported query parameters for the product search API.
 *
 * Standard params: keyword, categoryIds, minPrice, maxPrice, tags, page, size, sort, direction
 * Dynamic attribute filters (e.g. color=red&size=M) are captured in [attributes].
 */
data class ProductSearchRequest(
    val keyword: String? = null,
    val categoryIds: List<Long>? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val tags: List<String>? = null,

    /** Dynamic attributes, e.g. {"color": "red", "size": "M"}.
     *  These are matched against the product name / description as a simple
     *  keyword strategy, because the current schema has no separate attribute table.
     *  Replace with a proper EAV join once an attribute table is added. */
    val attributes: Map<String, String> = emptyMap(),

    val page: Int = 0,
    val size: Int = 20,
    val sort: String = "createdDate",
    val direction: String = "desc"
)
