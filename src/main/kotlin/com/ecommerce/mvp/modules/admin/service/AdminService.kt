package com.ecommerce.mvp.modules.admin.service
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.admin.dto.AdminInfoDto
import com.ecommerce.mvp.modules.admin.dto.DashboardStatsDto
import com.ecommerce.mvp.modules.admin.dto.LowStockProductDto
import com.ecommerce.mvp.modules.admin.dto.TopProductDto
import com.ecommerce.mvp.modules.order.repository.OrderRepository
import com.ecommerce.mvp.modules.product.repository.ProductRepository
import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.math.BigDecimal
private const val LOW_STOCK_THRESHOLD = 10
private const val TOP_PRODUCTS_LIMIT = 5
@Service
class DashboardService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {
    fun getDashboardStats(): DashboardStatsDto {
        val totalOrders = orderRepository.count()
        val totalCustomers = userRepository.count()
        val totalProducts = productRepository.countByIsActiveTrue()
        val totalRevenue: BigDecimal = orderRepository.sumTotalRevenue()
        val orderStatusBreakdown: Map<String, Long> = orderRepository
            .countByStatus()
            .associate { row -> row[0].toString() to (row[1] as Long) }
        val topSellingProducts: List<TopProductDto> = orderRepository
            .findTopSellingProducts(PageRequest.of(0, TOP_PRODUCTS_LIMIT))
            .map { row ->
                TopProductDto(
                    productId = row[0] as Long,
                    productName = row[1] as String,
                    totalQuantitySold = row[2] as Long,
                    totalRevenue = row[3] as BigDecimal
                )
            }
        val lowStockAlerts: List<LowStockProductDto> = productRepository
            .findLowStockProducts(LOW_STOCK_THRESHOLD)
            .map { product ->
                LowStockProductDto(
                    productId = product.id!!,
                    productName = product.name,
                    currentStock = product.stock
                )
            }
        return DashboardStatsDto(
            totalOrders = totalOrders,
            totalRevenue = totalRevenue,
            totalCustomers = totalCustomers,
            totalProducts = totalProducts,
            orderStatusBreakdown = orderStatusBreakdown,
            topSellingProducts = topSellingProducts,
            lowStockAlerts = lowStockAlerts
        )
    }

    fun getAdminInfo(): AdminInfoDto {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResourceNotFoundException("Authenticated admin not found")

        val admin = userRepository.findByUserEmail(email)
            ?: throw ResourceNotFoundException("Admin user not found for email: $email")

        return AdminInfoDto(
            id = admin.id,
            name = admin.name,
            email = admin.email,
            phone = admin.phone,
            roles = admin.userRoles.map { it.name.toString() }
        )
    }
}
