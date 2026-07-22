package com.ecommerce.mvp.security

import com.ecommerce.mvp.modules.user.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(userEmail: String): UserDetails {
        val user = userRepository.findByUserEmail(email = userEmail)
            ?: throw UsernameNotFoundException("User not found")


        val authorities: MutableList<GrantedAuthority?> = ArrayList()
        // Adding a fine-grained authority (permission)
       // authorities.add(SimpleGrantedAuthority("WRITE_PRIVILEGE"))

        /*Adding a coarse-grained authority (role)
         Map each role to a GrantedAuthority with the "ROLE_" prefix so Spring
        Security's hasRole() and hasAuthority() expressions work correctly.*/
        /*val authoritiesRole = user.userRoles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.name?.name}")
        }*/
        user.userRoles.map { role ->
            authorities.add(SimpleGrantedAuthority("$ROLE_PREFIX${role.name?.name}"))
        }
        /*return org.springframework.security.core.userdetails.User
            .withUsername(user.email)
            .password(user.password)
            .authorities(authorities)
            .build()*/

        return CustomUserDetails(
            id = user.id ?: -1,
            email = user.email ?: "",
            password = user.password ?: "",
            authorities = authorities.filterNotNull()
        )

    }
}