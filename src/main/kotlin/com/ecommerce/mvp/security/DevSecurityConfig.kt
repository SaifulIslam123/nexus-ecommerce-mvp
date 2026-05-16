package com.ecommerce.mvp.security

import com.ecommerce.mvp.common.config.SwaggerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

/**
 * DEV security filter chain.
 *
 * More permissive than PROD:
 *  - Swagger UI & API-docs endpoints are publicly accessible
 *  - H2-console frame options are relaxed (if ever used)
 *  - All other rules mirror production
 */
@Configuration
@Profile("dev")
class DevSecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val swaggerProperties: SwaggerProperties
) {

    @Bean
    fun devSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val publicPaths = (swaggerProperties.publicPaths + "/api/v1/auth/**").toTypedArray()

        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(*publicPaths).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .httpBasic(Customizer.withDefaults())
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // Relax X-Frame-Options for H2 console in dev (if needed)
            .headers { it.frameOptions { fo -> fo.sameOrigin() } }
            .addFilterAfter(jwtAuthFilter, BasicAuthenticationFilter::class.java)

        return http.build()
    }
}

