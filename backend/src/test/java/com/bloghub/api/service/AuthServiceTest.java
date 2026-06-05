package com.bloghub.api.service;

import com.bloghub.api.dto.JwtResponse;
import com.bloghub.api.dto.LoginRequest;
import com.bloghub.api.dto.LogoutRequest;
import com.bloghub.api.dto.RefreshTokenRequest;
import com.bloghub.api.dto.RegisterRequest;
import com.bloghub.api.dto.UserDto;
import com.bloghub.api.entity.RefreshToken;
import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.repository.RoleRepository;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.security.JwtTokenProvider;
import com.bloghub.api.service.outbox.OutboxService;
import com.bloghub.api.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService unit tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private OutboxService outboxService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private Role userRole;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Alice");
        registerRequest.setUsername("alice");
        registerRequest.setEmail("alice@example.com");
        registerRequest.setPassword("StrongPass@123");

        userRole = Role.builder().id(1L).name(Role.RoleName.ROLE_USER).build();

        savedUser = User.builder()
                .id(1L)
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    @DisplayName("register - should register user successfully")
    void register_success() {
        given(userRepository.existsByUsername("alice")).willReturn(false);
        given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
        given(roleRepository.findByName(Role.RoleName.ROLE_USER)).willReturn(Optional.of(userRole));
        given(passwordEncoder.encode("StrongPass@123")).willReturn("hashed");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        UserDto result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - should throw when username is already taken")
    void register_duplicateUsername() {
        given(userRepository.existsByUsername("alice")).willReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BlogApiException.class)
                .extracting(ex -> ((BlogApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("register - should throw when email is already registered")
    void register_duplicateEmail() {
        given(userRepository.existsByUsername("alice")).willReturn(false);
        given(userRepository.existsByEmail("alice@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BlogApiException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("login - should return JWT response on valid credentials")
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("alice");
        loginRequest.setPassword("StrongPass@123");

        Authentication authentication = mock(Authentication.class);
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash("hash")
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(tokenProvider.generateToken(authentication)).willReturn("mocked.jwt.token");
        given(tokenProvider.getJwtExpirationMs()).willReturn(900000L);
        given(userRepository.findByUsernameOrEmail("alice", "alice")).willReturn(Optional.of(savedUser));
        given(authentication.getAuthorities()).willReturn(java.util.Collections.emptyList());
        given(refreshTokenService.issueToken(savedUser, null))
                .willReturn(new RefreshTokenService.IssuedRefreshToken("refresh-token", refreshToken));

        JwtResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("login - should record failure on invalid credentials")
    void login_invalidCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("alice");
        loginRequest.setPassword("wrong-password");

        given(userRepository.findByUsernameOrEmail("alice", "alice")).willReturn(Optional.of(savedUser));
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new org.springframework.security.authentication.BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
        verify(userRepository).save(savedUser);
        assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("refreshToken - should rotate refresh token and return new token pair")
    void refreshToken_rotates() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash("new-hash")
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        given(refreshTokenService.rotate("old-refresh-token", "127.0.0.1"))
                .willReturn(new RefreshTokenService.IssuedRefreshToken("new-refresh-token", refreshToken));
        given(tokenProvider.generateTokenFromUsername("alice")).willReturn("new-access-token");
        given(tokenProvider.getJwtExpirationMs()).willReturn(900000L);

        JwtResponse response = authService.refreshToken(request, "127.0.0.1");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("logout - should blacklist access token and revoke refresh token")
    void logout_blacklistsAndRevokes() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");
        given(tokenProvider.getRemainingTtl("access-token")).willReturn(Duration.ofMinutes(10));

        authService.logout("Bearer access-token", request);

        verify(tokenBlacklistService).blacklist("access-token", Duration.ofMinutes(10));
        verify(refreshTokenService).revoke("refresh-token");
    }
}
