package com.bloghub.security;

import com.bloghub.config.BloghubProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final BloghubProperties props;
    private final SecretKey key;

    public JwtService(BloghubProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.security().jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(BloghubUserPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.security().jwt().expirationMinutes() * 60);

        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(
                        "uid", principal.getId(),
                        "role", principal.getRole().name()
                ))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

