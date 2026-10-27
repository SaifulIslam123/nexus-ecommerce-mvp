package com.ecommerce.mvp.modules.shopper.repository

import com.ecommerce.mvp.common.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ShopperRepository : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = ?1")
    fun findByUserEmail(email: String?): User?

    @Query("SELECT user FROM User user JOIN FETCH user.addresses address WHERE user.email = :email AND address.id = :addressId")
    fun findByUserEmailAndAddressId(email: String?, addressId: Long): User?

    @Query("SELECT user FROM User user JOIN FETCH user.addresses address WHERE user.email = :email")
    fun findByUserEmailWithAddresses(email: String?): User?

    fun existsByEmail(email: String): Boolean
    //fun findByIdAndUserIdAndDeletedAtIsNull(id: Long, userId: Long): Address?

}
