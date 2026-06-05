package com.bloghub.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Utility component for generating, parsing, and validating JWT tokens.
 * Uses HMAC-SHA256 signing with a configurable secret key.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generate a JWT token from a successfully authenticated principal.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername());
    }

    /**
     * Generate a JWT token directly from a username string.
     */
    public String generateTokenFromUsername(String username) {
        return buildToken(username);
    }

    /**
     * Extract the username (subject) claim from a JWT token.
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validate a JWT token — checks signature and expiration.
     * Returns false and logs the reason for any invalid token.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT token format: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT token claims string is empty: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("JWT processing error: {}", ex.getMessage());
        }
        return false;
    }

    public Duration getRemainingTtl(String token) {
        try {
            Instant expiresAt = parseClaims(token).getExpiration().toInstant();
            Duration ttl = Duration.between(Instant.now(), expiresAt);
            return ttl.isNegative() ? Duration.ZERO : ttl;
        } catch (JwtException | IllegalArgumentException ex) {
            return Duration.ZERO;
        }
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String buildToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
