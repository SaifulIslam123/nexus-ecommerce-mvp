package com.ecommerce.mvp.modules.order.repository

import com.ecommerce.mvp.modules.order.model.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long>
