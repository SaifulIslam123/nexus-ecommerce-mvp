package com.ecommerce.mvp.modules.user.service

import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.role.model.entity.ERole
import com.ecommerce.mvp.modules.role.model.entity.Role
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.model.dto.toEntity
import com.ecommerce.mvp.modules.user.model.entity.User
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.repository.RoleRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections

@Service
class UserService(
    private val userRepository: UserRepository,
    private val  roleRepository: RoleRepository
) {
    var dbRoleList: MutableList<Role> = Collections.emptyList();

    @PostConstruct
    fun init() {
        this.dbRoleList = roleRepository.findAll()
    }

    @Transactional
    fun createUser(user: User): User {

        val userRole = dbRoleList.find { it.name == ERole.USER }
        println("userRole: $userRole")

        //val roleUer = roleRepository.getReferenceById(1L)
        userRole?.let { user.userRoles = mutableSetOf(it) }
        return userRepository.save(user)

    }

    @Transactional
    fun registerUser(userDto: UserDto): UserDto {

        val requestRoleList: MutableList<Role> = Collections.emptyList()

        userDto.userRoles?.forEach { roleName ->
            dbRoleList.find { it.name?.name == roleName }?.let { requestRoleList.add(it) }
                ?: run {
                throw ResourceNotFoundException("Role not found: $roleName")
            }
        }
        val user = userDto.toEntity().also {
            it.userRoles = requestRoleList.toMutableSet()
        }
        val savedUser = userRepository.save(user)

        return userDto.also {
            it.id = savedUser.id
        }
    }





}
