package com.ecommerce.mvp.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

data class CustomUserDetails(
    val id: Long,
    val email: String,
    @get:JvmName("userPassword")
    val password: String,
    @get:JvmName("userAuthorities")
    val authorities: Collection<GrantedAuthority>
) : User(email, password, authorities)