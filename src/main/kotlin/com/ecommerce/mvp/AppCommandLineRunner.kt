package com.ecommerce.mvp

import com.ecommerce.mvp.common.seeder.CategoryTagProductDataSeederService
import com.ecommerce.mvp.modules.order.service.OrderService
import com.ecommerce.mvp.modules.product.service.ProductService
import com.ecommerce.mvp.modules.role.service.RoleService
import com.ecommerce.mvp.modules.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class AppCommandLineRunner : CommandLineRunner {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var categoryTagProductDataSeederService: CategoryTagProductDataSeederService

    override fun run(vararg args: String?) {

        // Step 1: Populate default roles first (USER, ADMIN, MODERATOR)
        //roleService.populateDefaultRoles()

        // Alternative: Insert roles only if empty
        // roleService.insertRolesIfEmpty()

        // Step 2: Seed dummy categories and products (skips if data already exists)
     //   categoryTagProductDataSeederService.seedIfEmpty()

        // Step 3: Insert users (depends on roles being present)
        // userService.insertDummyUsers()

        // Step 4: Insert orders (depends on users being present)
        //orderService.insertDummyOrders()

        //orderService.testOrder()

        //userService.findOrderTestOrphan()

    }
}