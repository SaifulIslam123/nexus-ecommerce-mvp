package com.ecommerce.mvp.modules.user.service

import com.ecommerce.mvp.modules.role.model.entity.ERole
import com.ecommerce.mvp.modules.role.model.entity.Role
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
    var roleList: MutableList<Role> = Collections.emptyList();

    @PostConstruct
    fun init() {
        this.roleList = roleRepository.findAll()
    }

    fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }

    fun deleteById(id: Long) {
        userRepository.deleteById(id)
    }

    @Transactional
    fun insertDummyUsers(): List<User> {
        val allRoles = roleRepository.findAll()

        // First, ensure roles exist (you'd typically have these pre-created)
        val userRole = allRoles.find{it.name == ERole.USER } //roleRepository.findByName(ERole.USER)
        val adminRole = allRoles.find{it.name == ERole.ADMIN } //roleRepository.findByName(ERole.ADMIN);
        val moderatorRole = allRoles.find{it.name == ERole.MODERATOR } //roleRepository.findByName(ERole.MODERATOR);

        val dummyUsers = listOf(
            User().apply {
                name = "John Doe"
                email = "john@example.com"
                password = "password123"
                phone = "1234567890"
                adminRole?.let { userRoles = mutableSetOf(it) }
            },
            User().apply {
                name = "Jane Smith"
                email = "jane@example.com"
                password = "password456"
                phone = "0987654321"
                moderatorRole?.let { userRoles = mutableSetOf(it) }
            },
            User().apply {
                name = "Bob Wilson"
                email = "bob@example.com"
                password = "password789"
                phone = "5551234567"
                userRole?.let { userRoles = mutableSetOf(it) }
            }
        )

        return userRepository.saveAll(dummyUsers)
    }


    fun registerUser(user: User, strRoles: Set<String>?): User {
        val roles: MutableSet<Role> = HashSet()

        if (strRoles == null) {
            val userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow { RuntimeException("Error: Role is not found.") }
            roles.add(userRole)
        } else {
            strRoles.forEach { role ->
                when (role) {
                    "admin", "ADMIN" -> {
                        val adminRole = roleRepository.findByName(ERole.ADMIN)
                            .orElseThrow { RuntimeException("Error: Role is not found.") }
                        roles.add(adminRole)
                    }
                    "mod", "MODERATOR" -> {
                        val modRole = roleRepository.findByName(ERole.MODERATOR)
                            .orElseThrow { RuntimeException("Error: Role is not found.") }
                        roles.add(modRole)
                    }
                    else -> {
                        val userRole = roleRepository.findByName(ERole.USER)
                            .orElseThrow { RuntimeException("Error: Role is not found.") }
                        roles.add(userRole)
                    }
                }
            }
        }
        user.userRoles = roles
        return userRepository.save(user)
    }

    @Transactional
    fun createUser(user: User): User {

        val userRole = roleList.find { it.name == ERole.USER }
        println("userRole: $userRole")

        //val roleUer = roleRepository.getReferenceById(1L)
        userRole?.let { user.userRoles = mutableSetOf(it) }
        return userRepository.save(user)

    }


    /*//@Transactional
    fun findUserOrderByEmail(email: String): Optional<User> {

        return userRepository.findByUserEmail(email)
    }*/




}
