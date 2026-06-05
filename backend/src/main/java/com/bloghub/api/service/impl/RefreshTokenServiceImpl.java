package com.bloghub.api.service.impl;

import com.bloghub.api.entity.RefreshToken;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.repository.RefreshTokenRepository;
import com.bloghub.api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public IssuedRefreshToken issueToken(User user, String clientIp) {
        String rawToken = randomToken();
        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000))
                .createdByIp(clientIp)
                .build();
        return new IssuedRefreshToken(rawToken, refreshTokenRepository.save(token));
    }

    @Override
    @Transactional
    public IssuedRefreshToken rotate(String rawRefreshToken, String clientIp) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new BlogApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (existing.isRevoked()) {
            throw new BlogApiException(HttpStatus.UNAUTHORIZED, "Refresh token has already been used or revoked");
        }
        if (existing.isExpired()) {
            throw new BlogApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        existing.setUsedAt(LocalDateTime.now());
        existing.setRevokedAt(LocalDateTime.now());

        IssuedRefreshToken replacement = issueToken(existing.getUser(), clientIp);
        existing.setReplacedByTokenHash(replacement.entity().getTokenHash());
        refreshTokenRepository.save(existing);
        return replacement;
    }

    @Override
    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    private String randomToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
