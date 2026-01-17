package com.grace.gracemanageservice.infrastructure.security;

import com.grace.gracemanageservice.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider - handles token generation and validation
 * Supports dual-token strategy: access token (15 min) + refresh token (7 days)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // Ensure secret is at least 256 bits (32 bytes) for HS256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate ACCESS token for authenticated user (short-lived: 15 minutes)
     * Contains full user claims: id, email, role
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("id", user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole())
            .claim("type", TOKEN_TYPE_ACCESS)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Generate REFRESH token for authenticated user (long-lived: 7 days)
     * Contains minimal claims: id, username only
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("id", user.getId())
            .claim("type", TOKEN_TYPE_REFRESH)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Get access token expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return ACCESS_TOKEN_EXPIRATION_MS / 1000;
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract token type from JWT token
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    /**
     * Check if token is an access token
     */
    public boolean isAccessToken(String token) {
        try {
            return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenType(token);
            boolean isRefresh = TOKEN_TYPE_REFRESH.equals(tokenType);
            if(!isRefresh) {
                log.error("Expected refresh token but found type: {}", tokenType);
            }
            return isRefresh;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse token and return claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Check if token is expired (throws ExpiredJwtException)
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return false;
        } catch (ExpiredJwtException ex) {
            log.warn("Token is expired");
            return true;
        } catch (Exception ex) {
            // For other exceptions, we'll handle them in validateToken
            return false;
        }
    }

    /**
     * Validate access token specifically (must be valid AND be access type)
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token) && isAccessToken(token);
    }

    /**
     * Validate refresh token specifically (must be valid AND be refresh type)
     */
    public boolean validateRefreshToken(String token) {
        return !validateToken(token) || !isRefreshToken(token);
    }
}

