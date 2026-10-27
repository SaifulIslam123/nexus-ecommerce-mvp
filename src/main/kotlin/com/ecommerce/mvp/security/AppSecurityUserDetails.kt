package com.ecommerce.mvp.security

import com.ecommerce.mvp.common.entity.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

val currentUserEntity: User?
    get() {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is AppSecurityUserDetails) principal.userEntity else null
    }

val appSecurityUserDetails: AppSecurityUserDetails?
    get() {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return principal as? AppSecurityUserDetails
    }

class AppSecurityUserDetails(
    val userEntity: User,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String? {
        return userEntity.password
    }

    override fun getUsername(): String? {
        return userEntity.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        //return userEntity.isEnabled()
        return true
    }

    fun getUserId(): Long {
        return userEntity.id ?: -1
    }
}
