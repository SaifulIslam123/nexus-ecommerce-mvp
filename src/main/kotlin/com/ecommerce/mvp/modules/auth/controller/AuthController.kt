package com.ecommerce.mvp.modules.auth.controller

import com.ecommerce.mvp.modules.auth.model.AuthRequest
import com.ecommerce.mvp.modules.auth.model.LoginResponseDto
import com.ecommerce.mvp.modules.user.model.dto.UserDto
import com.ecommerce.mvp.modules.user.repository.UserRepository
import com.ecommerce.mvp.modules.user.service.UserService
import com.ecommerce.mvp.security.JwtUtil
import com.ecommerce.mvp.security.TokenBlacklistService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): LoginResponseDto {
        // Authenticate the user — throws on bad credentials
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        val userDetails: UserDetails = authentication.principal as UserDetails

        val authorities: MutableList<String> = userDetails.authorities
            .map { it.authority }
            .toMutableList()

        jwtUtil.generateToken(request.username, authorities).also {
            return LoginResponseDto(it)
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserDto): UserDto {
        val userDto = userService.registerUser(request.apply {
            password = passwordEncoder.encode(password)
        })
        return userDto
    }

    /**
     * Logout: invalidates the bearer token by adding it to the blacklist.
     *
     * The client must send the same Authorization: Bearer <token> header.
     * Once blacklisted, the token is rejected by JwtAuthenticationFilter on
     * every subsequent request, even if it has not naturally expired yet.
     */
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): String {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            tokenBlacklistService.blacklist(token)
        }

        return "Logged out successfully"
    }
}


