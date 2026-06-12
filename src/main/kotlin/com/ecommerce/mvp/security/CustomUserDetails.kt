package com.ecommerce.mvp.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

data class CustomUserDetails(
    val id: Long,
    val email: String,
    val password: String,
    val authorities: Collection<GrantedAuthority>
) : User(email, password, authorities)