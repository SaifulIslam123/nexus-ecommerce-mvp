package com.ecommerce.mvp.security

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


    companion object {
        // IMPORTANT: This key should be strong (256-bit) and stored in properties!
        // This is a Base64 encoded sample key
        private const val SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437"
    }

    fun generateToken(userEmail: String): String {
        return Jwts.builder()
            .setSubject(userEmail)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
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
        val keyBytes = Decoders.BASE64.decode(SECRET)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}