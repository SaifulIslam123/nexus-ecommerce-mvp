package com.ecommerce.mvp.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val customerUserDetailsService: UserDetailsService,
    private val jwtAuthFilter: JwtAuthenticationFilter
) {

   /* @Bean
    fun passwordEncoder(): Pbkdf2PasswordEncoder {
        val algorithm = Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA1
        return Pbkdf2PasswordEncoder("secret, encrypt me ;)", 16, 128, algorithm)
    }*/

    /*@Bean
    fun authenticationManager(
        http: HttpSecurity,
        passwordEncoder: Pbkdf2PasswordEncoder
    ): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder
            .userDetailsService(customerUserDetailsService)
            .passwordEncoder(passwordEncoder)
        authenticationManagerBuilder.parentAuthenticationManager(null)
        return authenticationManagerBuilder.build()
    }*/

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                //  it.requestMatchers(AntPathRequestMatcher("**")).authenticated() // restrict all endpoints by default
                it.requestMatchers(AntPathRequestMatcher("/api/auth/**")).permitAll()  // Allow auth endpoints
                    //it.requestMatchers(AntPathRequestMatcher("**")).authenticated()
                    .anyRequest().authenticated()
            }
            .httpBasic(Customizer.withDefaults())
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterAfter(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
