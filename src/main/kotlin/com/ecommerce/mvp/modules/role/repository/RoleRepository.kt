package com.practice.ecommerce.ecommerce.repository

import com.practice.ecommerce.ecommerce.modules.role.model.entity.ERole
import com.practice.ecommerce.ecommerce.modules.role.model.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    fun findByName(name: ERole): Optional<Role>
}