package com.ecommerce.mvp.modules.user.repository

import com.practice.ecommerce.ecommerce.modules.user.model.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun findByUserId(userId: Long): List<Address>
}

