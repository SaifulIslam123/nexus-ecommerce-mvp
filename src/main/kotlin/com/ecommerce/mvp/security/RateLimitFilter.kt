package com.ecommerce.mvp.security

import com.ecommerce.mvp.common.exception.RateLimitException
import io.jsonwebtoken.Jwts
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    @Value("\${app.jwt.secret}") private val secretKey: String,
    @param:Qualifier("handlerExceptionResolver")
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    // These endpoints get the tight 5/min limit
    private val sensitiveEndpoints = listOf(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/forgot-password",
       // "/api/v1/auth/reset-password"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
        val clientIp = resolveClientIp(request)
        val matchedEndpoint = sensitiveEndpoints.firstOrNull { path.startsWith(it) }
        // Decide: which key and which limit to apply
        //val (rateLimitKey, limit, window) = when {
        val rateLimitInfo: RateLimitInfo = when {


            // Sensitive endpoints → always IP-based, tight limit
            /*sensitiveEndpoints.any { path.startsWith(it) } -> Triple(
                "sensitive:$clientIp",
                RateLimitConstants.SENSITIVE_LIMIT,
                RateLimitConstants.SENSITIVE_WINDOW_SECONDS
            )*/

            matchedEndpoint.isNullOrEmpty().not() -> {
                when (matchedEndpoint) {

                    "/api/v1/auth/forgot-password" -> {
                        val email = request.getParameter("email") ?: ""
                        val allowed = rateLimitService.isForgotPasswordAllowed(email, clientIp)
                        val currentCountEmail = rateLimitService.getCurrentCount("forgot_password_email:$email")
                        val currentCountIp = rateLimitService.getCurrentCount("forgot_password_ip:$clientIp")
                        RateLimitInfo(
                            allowed = allowed,
                            currentCount = maxOf(currentCountEmail, currentCountIp),
                            remaining = maxOf(0L, RateLimitConstants.FORGOT_PASSWORD_EMAIL_LIMIT - currentCountEmail
                            ).coerceAtMost(maxOf(0L, RateLimitConstants.FORGOT_PASSWORD_IP_LIMIT - currentCountIp)),
                            window = RateLimitConstants.FORGOT_PASSWORD_WINDOW_SECONDS,
                            limit = RateLimitConstants.FORGOT_PASSWORD_EMAIL_LIMIT.coerceAtMost(RateLimitConstants.FORGOT_PASSWORD_IP_LIMIT))
                    }

                    else -> {
                        val allowed = rateLimitService.isAllowed(
                            "sensitive:$clientIp",
                            RateLimitConstants.SENSITIVE_LIMIT,
                            RateLimitConstants.SENSITIVE_WINDOW_SECONDS
                        )
                        val currentCount = rateLimitService.getCurrentCount("sensitive:$clientIp")
                        RateLimitInfo(
                            allowed = allowed,
                            currentCount = currentCount,
                            remaining = maxOf(0L, RateLimitConstants.SENSITIVE_LIMIT - currentCount),
                            window = RateLimitConstants.SENSITIVE_WINDOW_SECONDS,
                            limit = RateLimitConstants.SENSITIVE_LIMIT
                        )
                    }
                }
            }


            // Authenticated request → use userId from JWT
            else -> {
                val userId = extractUserIdFromJwt(request)
                if (userId != null) {
                    RateLimitInfo(
                        allowed = rateLimitService.isAllowed("auth:$userId", RateLimitConstants.AUTH_LIMIT, RateLimitConstants.AUTH_WINDOW_SECONDS),
                        currentCount = rateLimitService.getCurrentCount("auth:$userId"),
                        remaining = maxOf(0L, RateLimitConstants.AUTH_LIMIT - rateLimitService.getCurrentCount("auth:$userId")),
                        window = RateLimitConstants.AUTH_WINDOW_SECONDS,
                        limit = RateLimitConstants.AUTH_LIMIT
                    )
                } else {
                    // No valid JWT → treat as public, IP-based
                    RateLimitInfo(
                        allowed = rateLimitService.isAllowed("public:$clientIp", RateLimitConstants.PUBLIC_LIMIT, RateLimitConstants.PUBLIC_WINDOW_SECONDS),
                        currentCount = rateLimitService.getCurrentCount("public:$clientIp"),
                        remaining = maxOf(0L, RateLimitConstants.PUBLIC_LIMIT - rateLimitService.getCurrentCount("public:$clientIp")),
                        window = RateLimitConstants.PUBLIC_WINDOW_SECONDS,
                        limit = RateLimitConstants.PUBLIC_LIMIT
                    )
                }
            }
        }

        /* val allowed = rateLimitService.isAllowed(rateLimitKey, limit, window)
         val currentCount = rateLimitService.getCurrentCount(rateLimitKey)
         val remaining = maxOf(0L, limit - currentCount)*/

        // Always send these headers — clients use them to self-throttle
        response.setHeader("X-RateLimit-Limit", rateLimitInfo.limit.toString())
        response.setHeader("X-RateLimit-Remaining", rateLimitInfo.remaining.toString())
        response.setHeader("X-RateLimit-Window-Seconds", rateLimitInfo.window.toString())

        if (!rateLimitInfo.allowed) {
            handlerExceptionResolver.resolveException(
                request,
                response,
                null,
                RateLimitException("Rate limit exceeded. Please try again later.")
            )
            return  // Stop here — don't pass to next filter
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extracts userId (subject) from the JWT token in Authorization header.
     * Returns null if no token or invalid token — caller falls back to IP.
     */
    private fun extractUserIdFromJwt(request: HttpServletRequest): String? {
        return try {
            val authHeader = request.getHeader("Authorization") ?: return null
            if (!authHeader.startsWith("Bearer ")) return null

            val token = authHeader.removePrefix("Bearer ")

            // Parse JWT and extract subject (userId or email — whatever you set as subject)
            Jwts.parserBuilder()
                .setSigningKey(secretKey.toByteArray())
                .build()
                .parseClaimsJws(token)
                .body
                .subject

        } catch (e: Exception) {
            // Expired, malformed, or tampered token → fall back to IP-based limiting
            null
        }
    }

    /**
     * Handles reverse proxy / load balancer scenarios.
     * X-Forwarded-For contains the real client IP when behind Nginx/AWS.
     */
    private fun resolveClientIp(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        return if (!forwardedFor.isNullOrBlank()) {
            forwardedFor.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }

}

private data class RateLimitInfo(
    val allowed: Boolean,
    val currentCount: Long,
    val remaining: Long,
    val window: Long,
    val limit: Long
)