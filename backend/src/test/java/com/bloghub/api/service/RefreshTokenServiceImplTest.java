package com.bloghub.api.service;

import com.bloghub.api.entity.RefreshToken;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.repository.RefreshTokenRepository;
import com.bloghub.api.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService unit tests")
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;
    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
        user = User.builder().id(1L).username("alice").email("alice@example.com").build();
    }

    @Test
    @DisplayName("issueToken - stores only hashed refresh token")
    void issueToken_hashesToken() {
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issueToken(user, "127.0.0.1");

        assertThat(issued.rawToken()).isNotBlank();
        assertThat(issued.entity().getTokenHash()).hasSize(64);
        assertThat(issued.entity().getTokenHash()).doesNotContain(issued.rawToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("rotate - rejects revoked refresh token")
    void rotate_revokedToken() {
        String rawToken = "raw-refresh-token";
        RefreshToken token = RefreshToken.builder()
                .tokenHash("ignored")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revokedAt(LocalDateTime.now())
                .build();
        given(refreshTokenRepository.findByTokenHash(any())).willReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.rotate(rawToken, "127.0.0.1"))
                .isInstanceOf(BlogApiException.class)
                .hasMessageContaining("revoked");
    }
}
