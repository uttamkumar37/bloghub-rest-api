package com.bloghub.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider unit tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    // Use a 256-bit base64-encoded key for HS256
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleUZvckpXVFRlc3RzT25seU5vdEZvclByb2R1Y3Rpb24xMjM=";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    @DisplayName("should generate a non-null token from authentication")
    void generateToken_fromAuthentication() {
        Authentication auth = buildAuthentication("alice");
        String token = tokenProvider.generateToken(auth);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should extract correct username from generated token")
    void getUsernameFromToken() {
        String token = tokenProvider.generateTokenFromUsername("alice");
        String username = tokenProvider.getUsernameFromToken(token);
        assertThat(username).isEqualTo("alice");
    }

    @Test
    @DisplayName("should validate a freshly generated token")
    void validateToken_valid() {
        String token = tokenProvider.generateTokenFromUsername("alice");
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("should reject a tampered token")
    void validateToken_tampered() {
        String token = tokenProvider.generateTokenFromUsername("alice");
        String tampered = token + "tampered";
        assertThat(tokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("should reject an obviously invalid token string")
    void validateToken_invalid() {
        assertThat(tokenProvider.validateToken("not.a.jwt")).isFalse();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Authentication buildAuthentication(String username) {
        UserDetails userDetails = new User(username, "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
