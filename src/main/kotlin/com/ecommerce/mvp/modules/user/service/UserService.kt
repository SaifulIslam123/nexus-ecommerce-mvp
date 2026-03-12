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
import com.ecommerce.mvp.modules.user.model.dto.UserProfileUpdateDto
import com.ecommerce.mvp.modules.user.model.dto.AddressDto
import com.ecommerce.mvp.modules.user.model.entity.Address
import com.ecommerce.mvp.modules.user.repository.AddressRepository
import org.springframework.security.core.context.SecurityContextHolder

@Service
class UserService(
    private val userRepository: UserRepository,
    private val  roleRepository: RoleRepository,
    private val addressRepository: AddressRepository
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

        val requestRoleList: MutableList<Role> = mutableListOf()



        userDto.userRoles?.forEach { roleName ->

            /*val role = roleRepository.findByName(ERole.valueOf(roleName))
                ?: throw ResourceNotFoundException("Role not found: $roleName")*/

            dbRoleList.find { it.name?.name == roleName }?.let { requestRoleList.add(
                roleRepository.getReferenceById(it.id ?: throw ResourceNotFoundException("Role reference id not found: $roleName"))
            ) }
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

    fun getCurrentUserProfile(): UserDto {
        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")
        val address = user.addresses.firstOrNull()
        val addressDto = address?.let {
            AddressDto(
                street = it.street,
                city = it.city,
                zip = it.zip,
                country = it.country
            )
        }
        return UserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            address = addressDto
        )
    }

    @Transactional
    fun updateCurrentUserProfile(updateDto: UserProfileUpdateDto): UserDto {
        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")

        updateDto.name?.takeIf { it.isNotBlank() }?.let { user.name = it }
        updateDto.phone?.takeIf { it.isNotBlank() }?.let { user.phone = it }

        updateDto.address?.let {
            val address = user.addresses.firstOrNull() ?: Address().apply { this.user = user }
            address.street = it.street
            address.city = it.city
            address.zip = it.zip
            address.country = it.country
            user.addresses.clear()
            user.addresses.add(address)
            addressRepository.save(address)
        }

        userRepository.save<User>(user)
        return getCurrentUserProfile()
    }





}
