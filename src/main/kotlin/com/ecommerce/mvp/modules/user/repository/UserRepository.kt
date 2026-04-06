package com.ecommerce.mvp.modules.user.repository

import com.ecommerce.mvp.modules.user.model.entity.Address
import com.ecommerce.mvp.modules.user.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = ?1")
    fun findByUserEmail(email: String?): User?

    @Query("SELECT user FROM User user JOIN FETCH user.addresses address WHERE user.email = :email AND address.id = :addressId")
    fun findByUserEmailAndAddressId(email: String?, addressId: Long): User?

    //fun findByIdAndUserIdAndDeletedAtIsNull(id: Long, userId: Long): Address?

}
