package com.ecommerce.mvp.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.*

@Component
class JwtUtil {

    @Value("\${app.jwt.secret}")
    private lateinit var secret: String

    @Value("\${app.jwt.expiration-ms}")
    private var expirationMs: Long = 36_000_000

    /**
     * Generates a signed JWT embedding the user's email as subject and their
     * roles as the standard "roles" claim.  Consumers can decode the token
     * without a DB round-trip to know which roles the bearer holds.
     */
    fun generateToken(userEmail: String, roles: MutableList<String> = mutableListOf()): String {
        return Jwts.builder()
            .setSubject(userEmail)
            .claim("roles", roles)           // e.g. ["ADMIN", "USER"]
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractUserEmail(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    /**
     * Reads the "roles" claim from the token and returns each value prefixed
     * with "ROLE_" so Spring Security's `hasRole()` / `hasAuthority()` checks
     * work correctly out of the box.
     */
    @Suppress("UNCHECKED_CAST")
    fun extractRoles(token: String): List<String> {
        val claims = extractAllClaims(token)
        val roles = claims["roles"] as? List<String> ?: emptyList()
        return roles.map { if (it.startsWith("ROLE_")) it else "ROLE_$it" }
    }

    fun validateToken(token: String, username: String): Boolean {
        val extractedUsername = extractUserEmail(token)
        return extractedUsername == username && !isTokenExpired(token)
    }

    /** Returns the expiration Date embedded in the token (used by the blacklist service). */
    fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractClaim(token, Claims::getExpiration).before(Date())
    }

    private fun getSignKey(): Key {
        val keyBytes = Decoders.BASE64.decode(secret)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}