package com.ecommerce.mvp.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

/**
 * PROD security filter chain.
 *
 * Stricter than DEV:
 *  - Swagger UI / API-docs paths are NOT permitted (disabled via properties anyway)
 *  - Only the auth endpoint is publicly accessible
 *  - HSTS and other secure headers are enforced
 */
@Configuration
@Profile("prod")
class ProdSecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: JwtAuthenticationEntryPoint
) {

    @Bean
    fun prodSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .httpBasic(Customizer.withDefaults())
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .headers {
                it.httpStrictTransportSecurity { hsts ->
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000)
                }
                it.contentTypeOptions(Customizer.withDefaults())
                it.frameOptions { fo -> fo.deny() }
            }
            .addFilterAfter(jwtAuthFilter, BasicAuthenticationFilter::class.java)

        return http.build()
    }
}

