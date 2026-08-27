package com.ecommerce.mvp.modules.order.repository

import com.ecommerce.mvp.modules.order.model.entity.Order
import com.ecommerce.mvp.modules.order.model.entity.OrderStatus
import com.ecommerce.mvp.modules.user.model.entity.User
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    fun findByOrderDateBetween(startDate: Instant, endDate: Instant): List<Order>


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

    //@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user u JOIN FETCH o.orderItems item JOIN FETCH item.product LEFT JOIN FETCH o.payment LEFT JOIN FETCH o.shipment WHERE u.email = :email ORDER BY o.orderDate DESC")
    fun findAllByUserEmail(email: String, pageable: Pageable): Page<Order>



    fun findByStatus(status: OrderStatus): List<Order>

    fun findByStatusIn(statuses: Collection<OrderStatus>): List<Order>

    @Query("SELECT o FROM Order o JOIN FETCH o.shipment s WHERE o.id = :orderId")
    fun findByIdWithShipment(orderId: Long): Order?

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = :new_status WHERE o.status = :order_status AND FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, o.orderDate) > 15")
    fun updateStatusForDeliveredOrdersOlderThan15Days(@Param("order_status") orderStatus: OrderStatus = OrderStatus.DELIVERED,
                                                      @Param("new_status") newStatus: OrderStatus = OrderStatus.RECEIVED)

    /**
     * Admin: Fetches all orders with their related data eagerly loaded.
     * DISTINCT prevents duplicates caused by the one-to-many JOIN on orderItems.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user JOIN FETCH o.orderItems item JOIN FETCH item.product LEFT JOIN FETCH o.payment LEFT JOIN FETCH o.shipment ORDER BY o.orderDate DESC")
    fun findAllOrdersWithDetails(): List<Order>

    /**
     * Admin: Fetches a single order by ID regardless of which user it belongs to.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.orderItems item JOIN FETCH item.product LEFT JOIN FETCH o.payment LEFT JOIN FETCH o.shipment WHERE o.id = :orderId")
    fun findOrderByIdCourier(@Param("orderId") orderId: Long): Order?

    /** Dashboard: total revenue from non-cancelled/failed orders. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status NOT IN (com.ecommerce.mvp.modules.order.model.entity.OrderStatus.CANCELLED, com.ecommerce.mvp.modules.order.model.entity.OrderStatus.FAILED)")
    fun sumTotalRevenue(): java.math.BigDecimal

    /** Dashboard: count of orders grouped by status. Returns [status, count] pairs. */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    fun countByStatus(): List<Array<Any>>

    /**
     * Dashboard: Top-selling products.
     * Returns rows of [productId, productName, totalQty, totalRevenue].
     */
    @Query("""
        SELECT item.product.id, item.product.name, SUM(item.quantity), SUM(item.price * item.quantity)
        FROM OrderItem item
        WHERE item.order.status NOT IN (
            com.ecommerce.mvp.modules.order.model.entity.OrderStatus.CANCELLED,
            com.ecommerce.mvp.modules.order.model.entity.OrderStatus.FAILED
        )
        GROUP BY item.product.id, item.product.name
        ORDER BY SUM(item.quantity) DESC
    """)
    fun findTopSellingProducts(pageable: Pageable): List<Array<Any>>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "javax.persistence.lock.timeout", value = "3000"))
    fun findOrderById(id: Long): Order?

    // 1. Sorts and paginates just the IDs in the database
    @Query("""SELECT o.id FROM Order o WHERE o.user = :user""",
        countQuery = """SELECT COUNT(o) FROM Order o WHERE o.user = :user""")
    fun findOrderIdsByUser(user: User, pageable: Pageable): Page<Long>?

    // 2. Fetches collections for those IDs, maintaining the sorted order
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems item LEFT JOIN FETCH item.product LEFT JOIN FETCH o.payment payment LEFT JOIN FETCH o.shipment shipment WHERE o.id IN :ids")
    fun findOrdersByIds(@Param("ids") ids: MutableList<Long>, sort: Sort): MutableList<Order>


}
