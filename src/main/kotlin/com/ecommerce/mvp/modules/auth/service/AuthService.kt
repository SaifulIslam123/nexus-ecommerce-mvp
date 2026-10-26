package com.ecommerce.mvp.modules.auth.service

import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.auth.model.AuthRequest
import com.ecommerce.mvp.modules.auth.model.LoginResponseDto
import com.ecommerce.mvp.modules.auth.model.RefreshRequest
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.security.AppSecurityUserDetails
import com.ecommerce.mvp.security.JwtUtil
import com.ecommerce.mvp.security.appSecurityUserDetails
import com.ecommerce.mvp.security.currentUserEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val jwtUtil: JwtUtil,
) {

    @Transactional
    fun login(request: AuthRequest): LoginResponseDto {


        appSecurityUserDetails?.let { user->

            val authorities = user.authorities.map { it.authority }.toMutableList()
            val accessToken = jwtUtil.generateAccessToken(request.userEmail!!, authorities)

            val refreshToken = refreshTokenService.createRefreshToken(user)

            return LoginResponseDto(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = jwtUtil.getAccessTokenExpirationMs() / 1000
            )
        }

        throw ResourceNotFoundException("Authenticated user not found")

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