package com.ecommerce.mvp.modules.admin

import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.admin.dto.AdminInfoDto
import com.ecommerce.mvp.modules.admin.dto.DashboardStatsDto
import com.ecommerce.mvp.modules.admin.service.DashboardService
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.modules.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val dashboardService: DashboardService,
    private val userService: UserService,
) {

    /**
     * GET /api/admin/dashboard/stats
     *
     * Returns aggregated statistics: total orders, revenue, customers,
     * active products, order status breakdown, top-selling products,
     * and low-stock alerts.
     */
    @GetMapping("/dashboard/stats")
    fun getDashboardStats(): DashboardStatsDto = dashboardService.getDashboardStats()

    /**
     * GET /api/admin/dashboard/me
     *
     * Returns the basic profile of the currently authenticated admin:
     * id, name, email, phone, and assigned roles.
     */
    @GetMapping("/dashboard/me")
    fun getAdminInfo(): AdminInfoDto = dashboardService.getAdminInfo()
    /**
     * GET /api/admin/users
     *
     * Returns a list of all registered users in the system.
     * Includes each user's profile, roles, and non-deleted addresses.
     */
    @GetMapping("/users")
    fun getAllUsers(): List<UserDto>  = userService.getAllUsers()

    /**
     * GET /api/admin/users/{id}
     *
     * Returns the full profile of a single user by their ID.
     *
     * Responds with 404 if the user does not exist.
     */
    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): UserDto  =  userService.getUserById(id)

    /**
     * DELETE /api/admin/users/{id}
     *
     * Permanently removes a user and all their associated data
     * (orders, cart, addresses — all cascaded by the entity mappings).
     *
     * Use with caution. Responds with 204 No Content on success.
     * Responds with 404 if the user does not exist.
     */
    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        userService.deleteUser(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }


}

