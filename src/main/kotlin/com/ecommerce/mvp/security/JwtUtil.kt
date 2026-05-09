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

    fun generateToken(userEmail: String): String {
        return Jwts.builder()
            .setSubject(userEmail)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractUserEmail(token: String): String {
        return extractClaim(token, Claims::getSubject)
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