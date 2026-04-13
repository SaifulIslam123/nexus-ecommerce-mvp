package com.ecommerce.mvp.schedulers

import com.ecommerce.mvp.modules.order.repository.OrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OrderScheduler {

    @Autowired
    private lateinit var orderRepository: OrderRepository


    /**
     * Runs every day
     *
     * Scheduled task that runs every day at midnight (00:00:00).
     * Deletes all delivered orders that are older than 15 days
     * to keep the database clean from stale order records.
     */
    @Scheduled(cron = "0 0 0 * * *")
    fun purgeDeliveredOrdersOlderThan15Days() {
        orderRepository.updateStatusForDeliveredOrdersOlderThan15Days()
    }
}