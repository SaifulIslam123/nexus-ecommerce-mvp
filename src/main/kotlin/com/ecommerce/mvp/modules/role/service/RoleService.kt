package com.ecommerce.mvp.modules.role.service

import com.ecommerce.mvp.modules.role.model.entity.Role
import com.ecommerce.mvp.repository.RoleRepository
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {

    fun findById(id: Long): Role? {
        return roleRepository.findById(id).orElse(null)
    }

    fun saveRole(role: Role): Role {
        return roleRepository.save(role)
    }
}

