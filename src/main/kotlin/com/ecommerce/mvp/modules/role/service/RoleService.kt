package com.ecommerce.mvp.modules.role.service

import com.ecommerce.mvp.modules.role.model.entity.ERole
import com.ecommerce.mvp.modules.role.model.entity.Role
import com.ecommerce.mvp.repository.RoleRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections.emptyList

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {

    fun findById(id: Long): Role? {
        return roleRepository.findById(id).orElse(null)
    }

    fun findByName(name: ERole): Role? {
        return roleRepository.findByName(name).orElse(null)
    }

    fun findAll(): List<Role> {
        return roleRepository.findAll()
    }

    fun save(role: Role): Role {
        return roleRepository.save(role)
    }

    fun deleteById(id: Long) {
        roleRepository.deleteById(id)
    }

    /**
     * Populates the database with default roles (USER, ADMIN, MODERATOR)
     * This method checks if roles already exist before inserting them
     * to avoid duplicate entries.
     */
    @Transactional
    fun populateDefaultRoles(): List<Role> {
        val roles = mutableListOf<Role>()

        // Check if USER role exists, if not create it
        if (findByName(ERole.USER) == null) {
            val userRole = Role().apply {
                name = ERole.USER
            }
            roles.add(save(userRole))
            println("Created USER role")
        } else {
            println("USER role already exists")
        }

        // Check if ADMIN role exists, if not create it
        if (findByName(ERole.ADMIN) == null) {
            val adminRole = Role().apply {
                name = ERole.ADMIN
            }
            roles.add(save(adminRole))
            println("Created ADMIN role")
        } else {
            println("ADMIN role already exists")
        }

        // Check if MODERATOR role exists, if not create it
        if (findByName(ERole.MODERATOR) == null) {
            val moderatorRole = Role().apply {
                name = ERole.MODERATOR
            }
            roles.add(save(moderatorRole))
            println("Created MODERATOR role")
        } else {
            println("MODERATOR role already exists")
        }

        println("Total roles in database: ${findAll().size}")
        return roles
    }

    /**
     * Inserts roles only if the database is empty.
     * This is useful for initial setup and prevents duplicate entries.
     */
    @Transactional
    fun insertRolesIfEmpty(): List<Role> {
        val existingRoles = findAll()

        if (existingRoles.isNotEmpty()) {
            println("Roles already exist in the database. Total: ${existingRoles.size}")
            return existingRoles
        }

        return populateDefaultRoles()
    }

    /**
     * Creates and saves a single role with the given ERole type
     */
    @Transactional
    fun createRole(roleType: ERole): Role? {
        return if (findByName(roleType) == null) {
            val newRole = Role().apply {
                name = roleType
            }
            save(newRole).also {
                println("Created $roleType role with ID: ${it.id}")
            }
        } else {
            println("$roleType role already exists")
            findByName(roleType)
        }
    }

    /**
     * Retrieves all roles with their associated users count
     */
    fun getAllRolesWithDetails(): List<Pair<Role, String>> {
        return findAll().map { role ->
            role to "Role: ${role.name}"
        }
    }
}

