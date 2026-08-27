package com.ecommerce.mvp.security

import com.ecommerce.mvp.modules.user.model.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails


/*data class CustomUserDetails(
    val id: Long,
    val email: String,
    @get:JvmName("userPassword")
    val password: String,
    @get:JvmName("userAuthorities")
    val authorities: Collection<GrantedAuthority>
) : User(email, password, authorities)*/


val currentUserEntity: User?
    get() {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is AppSecurityUserDetails) principal.userEntity else null
    }

class AppSecurityUserDetails(userEntity: User, authorities: Collection<GrantedAuthority>) : UserDetails {
    // Wrap your actual JPA / database Entity here
    var userEntity: User
        // Expose a getter to retrieve the full object in your services
        get() = this.userEntity
    private val authorities: Collection<GrantedAuthority>

    init {
        this.userEntity = userEntity
        this.authorities = authorities
    }

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

