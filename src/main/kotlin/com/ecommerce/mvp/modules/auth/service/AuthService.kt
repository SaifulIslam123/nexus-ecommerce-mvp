package com.ecommerce.mvp.modules.auth.service

import com.ecommerce.mvp.modules.auth.model.AuthRequest
import com.ecommerce.mvp.modules.auth.model.LoginResponseDto
import com.ecommerce.mvp.modules.auth.model.RefreshRequest
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.security.CustomUserDetails
import com.ecommerce.mvp.security.JwtUtil
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestBody

class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val jwtUtil: JwtUtil,
) {

    @Transactional
    fun login(request: AuthRequest): LoginResponseDto {
        //val userDetails: UserDetails = authentication.principal as UserDetails

        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails

        val authorities = userDetails.authorities.map { it.authority }.toMutableList()
        val accessToken = jwtUtil.generateAccessToken(request.username, authorities)

        val refreshToken = refreshTokenService.createRefreshToken(userDetails)

        return LoginResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtUtil.getAccessTokenExpirationMs() / 1000
        )
    }

    @Transactional
    fun refresh(body: RefreshRequest): LoginResponseDto {
        val result = refreshTokenService.rotateRefreshToken(body.refreshToken)
        val accessToken = jwtUtil.generateAccessToken(result.userEmail, result.roles.toMutableList())
        return LoginResponseDto(
            accessToken = accessToken,
            refreshToken = result.newRefreshTokenId,
            expiresIn = jwtUtil.getAccessTokenExpirationMs() / 1000
        )
    }
}