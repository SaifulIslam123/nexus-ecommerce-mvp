package com.ecommerce.mvp.modules.user.repository

import com.ecommerce.mvp.modules.user.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = ?1")
    fun findByUserEmail(email: String?): User?


}
