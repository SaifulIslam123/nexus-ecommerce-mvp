package com.ecommerce.mvp.modules.order.repository

import com.ecommerce.mvp.modules.order.model.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    /**
     * Finds an order by its ID, but only if it belongs to the authenticated user.
     * The JOIN FETCH on orderItems eagerly loads all line items in a single query,
     * avoiding N+1 issues when building the response DTO.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user u JOIN FETCH o.orderItems item JOIN FETCH item.product LEFT JOIN FETCH o.payment payment LEFT JOIN FETCH o.shipment shipment WHERE u.email = :email AND o.id = :orderId")
    fun findByIdAndUserEmail(orderId: Long, email: String): Order?
}
