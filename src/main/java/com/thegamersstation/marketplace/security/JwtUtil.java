package com.thegamersstation.marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access token
     */
    public String generateAccessToken(Long userId, String phoneNumber, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phoneNumber);
        claims.put("role", role);
        claims.put("type", "access");
        
        return createToken(claims, userId.toString(), accessTokenExpiration);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        return createToken(claims, userId.toString(), refreshTokenExpiration);
    }

    /**
     * Create JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username (user ID) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return Long.parseLong(subject);
    }

    /**
     * Extract phone number from token
     */
    public String extractPhone(String token) {
        return extractClaim(token, claims -> claims.get("phone", String.class));
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract token type from token
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token, Long userId) {
        try {
            final Long tokenUserId = extractUserId(token);
            return (tokenUserId.equals(userId) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Validate token type
     */
    public Boolean validateAccessToken(String token) {
        try {
            String type = extractTokenType(token);
            return "access".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Access token validation failed", e);
            return false;
        }
    }

    /**
     * Validate refresh token type
     */
    public Boolean validateRefreshToken(String token) {
        try {
            String type = extractTokenType(token);
            return "refresh".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Refresh token validation failed", e);
            return false;
        }
    }

    /**
     * Get access token expiration in milliseconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Get refresh token expiration in milliseconds
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
