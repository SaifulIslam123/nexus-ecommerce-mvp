package com.ecommerce.mvp.common.cache

/**
 * Central registry of all Redis cache names used in the application.
 *
 * Naming convention: <module>:<scope>
 * TTL is configured per-cache in [RedisConfig].
 */
object CacheNames {

    // ── Products ────────────────────────────────────────────────────────────
    /** Individual product by ID. TTL: 10 min */
    const val PRODUCTS = "products"

    /** Paginated / filtered product search results. TTL: 5 min */
    const val PRODUCTS_SEARCH = "products:search"

    /** Recommended products for a given product ID. TTL: 10 min */
    const val PRODUCTS_RECOMMENDATIONS = "products:recommendations"

    // ── Categories ──────────────────────────────────────────────────────────
    /** Full category tree (all roots + children). TTL: 30 min */
    const val CATEGORIES = "categories"

    /** Single category by ID. TTL: 30 min */
    const val CATEGORIES_SINGLE = "categories:single"

    // ── Tags ────────────────────────────────────────────────────────────────
    /** Paginated tag list. TTL: 30 min */
    const val TAGS = "tags"

    /** Single tag by ID. TTL: 30 min */
    const val TAGS_SINGLE = "tags:single"
}

