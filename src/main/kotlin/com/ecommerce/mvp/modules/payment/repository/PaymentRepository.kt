package com.ecommerce.mvp.modules.payment.repository

import com.ecommerce.mvp.modules.payment.model.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
}