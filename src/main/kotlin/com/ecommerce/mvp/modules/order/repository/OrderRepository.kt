package com.ecommerce.mvp.modules.order.repository

import com.ecommerce.mvp.modules.order.model.dto.OrderResponseDto
import com.ecommerce.mvp.modules.order.model.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    fun findByOrderDate( date: Date): List<Order>

   // fun findByIdInOrderDateBetween(orderId: Long, startDate: Date, endDate: Date): List<Order>


    /**
     * Finds an order by its ID, but only if it belongs to the authenticated user.
     * The JOIN FETCH on orderItems eagerly loads all line items in a single query,
     * avoiding N+1 issues when building the response DTO.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user u JOIN FETCH o.orderItems item JOIN FETCH item.product LEFT JOIN FETCH o.payment payment LEFT JOIN FETCH o.shipment shipment WHERE u.email = :email AND o.id = :orderId")
    fun findByIdAndUserEmail(orderId: Long, email: String): Order?

    /**
     * Returns all orders that belong to the given user, ordered by date
     * (newest first). DISTINCT prevents duplicate Order rows that would
     * otherwise arise from the one-to-many JOIN FETCH on orderItems.
     * JOIN FETCH is used for orderItems and product because every order
     * is guaranteed to have at least one item and every item always has
     * a product. payment and shipment use LEFT JOIN FETCH since they are
     * optional associations.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user u JOIN FETCH o.orderItems item JOIN FETCH item.product LEFT JOIN FETCH o.payment LEFT JOIN FETCH o.shipment WHERE u.email = :email ORDER BY o.orderDate DESC")
    fun findAllByUserEmail(email: String): List<Order>
}
