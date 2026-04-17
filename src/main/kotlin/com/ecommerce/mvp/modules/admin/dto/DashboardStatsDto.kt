package com.ecommerce.mvp.modules.admin.dto

import java.math.BigDecimal

data class DashboardStatsDto(
    val totalOrders: Long,
    val totalRevenue: BigDecimal,
    val totalCustomers: Long,
    val totalProducts: Long,
    val orderStatusBreakdown: Map<String, Long>,
    val topSellingProducts: List<TopProductDto>,
    val lowStockAlerts: List<LowStockProductDto>
)

data class TopProductDto(
    val productId: Long,
    val productName: String,
    val totalQuantitySold: Long,
    val totalRevenue: BigDecimal
)

data class LowStockProductDto(
    val productId: Long,
    val productName: String,
    val currentStock: Int
)

/** Basic profile of the currently authenticated admin shown at the top of the dashboard. */
data class AdminInfoDto(
    val id: Long?,
    val name: String?,
    val email: String?,
    val phone: String?,
    val roles: List<String>
)
