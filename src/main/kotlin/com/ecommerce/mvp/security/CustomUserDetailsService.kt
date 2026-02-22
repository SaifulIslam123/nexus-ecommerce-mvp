package com.ecommerce.mvp.security

import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(userEmail: String): UserDetails {
        val user = userRepository.findByUserEmail(email = userEmail) ?:
        throw UsernameNotFoundException("User not found")


        return org.springframework.security.core.userdetails.User
            .withUsername(user.email)
            .password("{noop}${user.password}")
            //.roles(user.userRoles.)
            .build()
    }
}