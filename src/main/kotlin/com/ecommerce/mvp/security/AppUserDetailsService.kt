package com.ecommerce.mvp.security

import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class AppSecurityUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(userEmail: String): UserDetails {
        val user = userRepository.findByUserEmail(email = userEmail)
            ?: throw UsernameNotFoundException("User not found")


        val authorities: MutableList<GrantedAuthority?> = ArrayList()
        user.userRoles.map { role ->
            authorities.add(SimpleGrantedAuthority("$ROLE_PREFIX${role.name?.name}"))
        }

        return AppSecurityUserDetails(user, authorities.filterNotNull())

    }
}