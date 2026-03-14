package com.ecommerce.mvp.modules.user.repository

import com.ecommerce.mvp.modules.user.model.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun findByIdAndUserIdAndDeletedAtIsNull(id: Long, userId: Long): Optional<Address>
}

