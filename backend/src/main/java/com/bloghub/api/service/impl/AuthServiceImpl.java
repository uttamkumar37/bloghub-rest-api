package com.bloghub.api.service.impl;

import com.bloghub.api.dto.JwtResponse;
import com.bloghub.api.dto.LoginRequest;
import com.bloghub.api.dto.LogoutRequest;
import com.bloghub.api.dto.RefreshTokenRequest;
import com.bloghub.api.dto.RegisterRequest;
import com.bloghub.api.dto.UserDto;
import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.repository.RoleRepository;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.security.JwtTokenProvider;
import com.bloghub.api.service.AuthService;
import com.bloghub.api.service.RefreshTokenService;
import com.bloghub.api.service.TokenBlacklistService;
import com.bloghub.api.service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication service implementation.
 * Handles user registration and JWT-based login.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration ACCOUNT_LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final OutboxService outboxService;

    /**
     * Registers a new user with ROLE_USER.
     * Validates uniqueness of username and email before persisting.
     */
    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BlogApiException(HttpStatus.BAD_REQUEST,
                    "Username '" + request.getUsername() + "' is already taken");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BlogApiException(HttpStatus.BAD_REQUEST,
                    "Email '" + request.getEmail() + "' is already registered");
        }

        // Lookup or create ROLE_USER
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(Role.RoleName.ROLE_USER).build()));

        // Build and persist the user entity
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(user);
        outboxService.saveEvent(
                "USER",
                String.valueOf(saved.getId()),
                "EMAIL_VERIFICATION_REQUESTED",
                "{\"userId\":" + saved.getId() + ",\"email\":\"" + saved.getEmail() + "\"}"
        );
        log.info("New user registered: {} ({})", saved.getUsername(), saved.getEmail());

        return mapToDto(saved);
    }

    /**
     * Authenticates the user and returns a signed JWT token.
     */
    @Override
    public JwtResponse login(LoginRequest request) {
        return login(request, null);
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request, String clientIp) {
        assertNotLocked(request.getUsernameOrEmail());

        Authentication authentication;
        try {
            // Delegate authentication to Spring Security
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            recordFailedLogin(request.getUsernameOrEmail());
            throw ex;
        }

        recordSuccessfulLogin(request.getUsernameOrEmail());
        // Delegate authentication to Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT
        String token = tokenProvider.generateToken(authentication);

        // Load user for response payload
        User user = userRepository.findByUsernameOrEmail(
                request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BlogApiException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found after authentication"));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issueToken(user, clientIp);
        log.info("User logged in: {}", user.getUsername());

        return JwtResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.rawToken())
                .tokenType("Bearer")
                .accessTokenExpiresInMs(tokenProvider.getJwtExpirationMs())
                .refreshTokenExpiresInMs(refreshToken.entity().getExpiresAt()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli() - System.currentTimeMillis())
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request, String clientIp) {
        RefreshTokenService.IssuedRefreshToken rotated = refreshTokenService.rotate(request.getRefreshToken(), clientIp);
        User user = rotated.entity().getUser();
        String accessToken = tokenProvider.generateTokenFromUsername(user.getUsername());
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rotated.rawToken())
                .tokenType("Bearer")
                .accessTokenExpiresInMs(tokenProvider.getJwtExpirationMs())
                .refreshTokenExpiresInMs(rotated.entity().getExpiresAt()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli() - System.currentTimeMillis())
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader, LogoutRequest request) {
        String accessToken = extractBearerToken(authorizationHeader);
        if (StringUtils.hasText(accessToken)) {
            tokenBlacklistService.blacklist(accessToken, tokenProvider.getRemainingTtl(accessToken));
        }
        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            refreshTokenService.revoke(request.getRefreshToken());
        }
    }

    // ── Mapping helper ─────────────────────────────────────────────────────────

    private void assertNotLocked(String usernameOrEmail) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .filter(User::isLoginLocked)
                .ifPresent(user -> {
                    throw new BlogApiException(HttpStatus.LOCKED,
                            "Account is temporarily locked due to repeated failed login attempts");
                });
    }

    private void recordFailedLogin(String usernameOrEmail) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plus(ACCOUNT_LOCK_DURATION));
                log.warn("User account locked after repeated failed login attempts: {}", user.getUsername());
            }
            userRepository.save(user);
        });
    }

    private void recordSuccessfulLogin(String usernameOrEmail) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
            if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        });
    }

    private String extractBearerToken(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private UserDto mapToDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
