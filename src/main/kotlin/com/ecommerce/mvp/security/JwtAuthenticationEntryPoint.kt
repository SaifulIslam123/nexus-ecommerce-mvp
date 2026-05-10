package com.ecommerce.mvp.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * Invoked by Spring Security whenever an unauthenticated request hits a
 * protected endpoint (missing token, blacklisted token, expired token, etc.)
 *
 * Instead of writing the JSON response directly here, we delegate to
 * HandlerExceptionResolver which forwards the exception into Spring MVC's
 * exception handling pipeline — so GlobalExceptionHandler handles it.
 */
@Component
class JwtAuthenticationEntryPoint(
    // "handlerExceptionResolver" is the default composite resolver bean name
    @param:Qualifier("handlerExceptionResolver")
    private val resolver: HandlerExceptionResolver
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // Hand the exception off to GlobalExceptionHandler via Spring MVC
        resolver.resolveException(request, response, null, authException)
    }
}
