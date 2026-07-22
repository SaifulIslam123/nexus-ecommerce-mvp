package com.ecommerce.mvp.modules.user.service

import com.ecommerce.mvp.common.AppConstant.MAX_Address
import com.ecommerce.mvp.common.AppConstant.MIN_Address
import com.ecommerce.mvp.common.exception.BusinessValidationException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.role.model.entity.Role
import com.ecommerce.mvp.modules.user.model.dto.AddressDto
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.model.dto.toEntity
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.modules.role.repository.RoleRepository
import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.ecommerce.mvp.modules.user.model.dto.UserProfileUpdateDto
import com.ecommerce.mvp.modules.user.model.dto.toUserDto
import com.ecommerce.mvp.modules.user.model.entity.Address
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.Collections

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
) {
    var dbRoleList: MutableList<Role> = Collections.emptyList();

    @PostConstruct
    fun init() {
        this.dbRoleList = roleRepository.findAll()
    }

    @Transactional
    fun registerUser(userDto: UserDto): UserDto {

        val requestRoleList: MutableList<Role> = mutableListOf()



        userDto.userRoles?.forEach { roleName ->

            /*val role = roleRepository.findByName(ERole.valueOf(roleName))
                ?: throw ResourceNotFoundException("Role not found: $roleName")*/

            dbRoleList.find { it.name?.name == roleName }?.let {
                requestRoleList.add(
                    roleRepository.getReferenceById(
                        it.id ?: throw ResourceNotFoundException("Role reference id not found: $roleName")
                    )
                )
            }
                ?: run {
                    throw ResourceNotFoundException("Role not found: $roleName")
                }
        }
        val user = userDto.toEntity().also {
            it.addresses = userDto.address?.map { addrDto ->
                Address().apply {
                    street = addrDto.street
                    city = addrDto.city
                    zip = addrDto.zip
                    country = addrDto.country
                    this.user = it
                }
            }?.toMutableSet() ?: mutableSetOf()
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

        return user.toUserDto()
    }

    @Transactional
    fun updateCurrentUserProfile(updateDto: UserProfileUpdateDto): UserDto {
        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")

        updateDto.name?.takeIf { it.isNotBlank() }?.let { user.name = it }
        updateDto.phone?.takeIf { it.isNotBlank() }?.let { user.phone = it }

        updateDto.address?.let { updateDtoAddresses ->

            if (user.addresses.size == MAX_Address) {
                throw BusinessValidationException("Maximum $MAX_Address addresses allowed. Please remove an existing address before adding a new one.")
            } else if ((user.addresses.size + updateDtoAddresses.size) > MAX_Address) {
                throw BusinessValidationException("Only ${MAX_Address - user.addresses.size} addresses can add")
            } else {
                for (it in updateDtoAddresses) {
                    val address = Address().apply { this.user = user }
                    address.street = it.street
                    address.city = it.city
                    address.zip = it.zip
                    address.country = it.country

                    user.addresses.add(address)
                }
            }
        }

        return user.toUserDto()
    }

    @Transactional
    fun addAddress(addressRequestDto: AddressDto): UserDto {
        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("User not found")

        if (user.addresses.size >= MAX_Address) {
            throw BusinessValidationException("Maximum $MAX_Address addresses allowed. Please remove an existing address before adding a new one.")
        }

        val address = Address().apply {
            street = addressRequestDto.street
            city = addressRequestDto.city
            zip = addressRequestDto.zip
            country = addressRequestDto.country
            this.user = user
        }

        user.addresses.add(address)
        val savedUser = userRepository.save(user)

        return savedUser.toUserDto()
    }

    @Transactional
    fun deleteAddress(addressId: Long) {
        val email = SecurityContextHolder.getContext().authentication?.name
        val user = userRepository.findByUserEmailWithAddresses(email)
        user?.let {
            user.addresses.takeIf { it.isNotEmpty() }?.let {

                if (it.size == MIN_Address) {
                    throw BusinessValidationException("At least $MIN_Address address is required. Please add a new address before removing the existing one.")
                }

                val address = it.find { addr -> addr.id == addressId }
                address?.let { addr ->
                    addr.deletedAt = LocalDateTime.now()
                } ?: throw ResourceNotFoundException("Address not found")
            } ?: throw ResourceNotFoundException("No addresses found for the user")

            //return user.toUserDto()
        }?: throw ResourceNotFoundException("User not found")

    }

    // ── Admin operations ──────────────────────────────────────────────────────

    /**
     * Admin: Returns every user registered in the system.
     */
    fun getAllUsers(page: Int = 0, size: Int = 10, sort: String = "id", direction: String = "ASC"): Page<UserDto> {
        val pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.uppercase()), sort)
        return userRepository.findAll(pageable).map { it.toUserDto() }
    }

    /**
     * Admin: Returns a single user by their ID.
     * Throws [ResourceNotFoundException] if the user does not exist.
     */
    fun getUserById(id: Long): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }
        return user.toUserDto()
    }

    /**
     * Admin: Permanently deletes a user and all their associated data
     * (cascaded by the entity mappings).
     * Throws [ResourceNotFoundException] if the user does not exist.
     */
    @Transactional
    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User not found with id: $id")
        }
        userRepository.deleteById(id)
    }

}
