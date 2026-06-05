package com.bloghub.api.controller;

import com.bloghub.api.dto.ApiResponse;
import com.bloghub.api.dto.JwtResponse;
import com.bloghub.api.dto.LoginRequest;
import com.bloghub.api.dto.LogoutRequest;
import com.bloghub.api.dto.RefreshTokenRequest;
import com.bloghub.api.dto.RegisterRequest;
import com.bloghub.api.dto.UserDto;
import com.bloghub.api.service.RateLimiterService;
import com.bloghub.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Authentication endpoints — register and login.
 * These endpoints are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    @Value("${app.rate-limit.auth.login.limit:10}")
    private int loginLimit;

    @Value("${app.rate-limit.auth.register.limit:5}")
    private int registerLimit;

    @Value("${app.rate-limit.auth.window-seconds:60}")
    private int rateLimitWindowSeconds;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletRequest servletRequest) {
        rateLimiterService.checkAllowed("auth:register:" + clientIp(servletRequest),
                registerLimit, Duration.ofSeconds(rateLimitWindowSeconds));
        UserDto user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT access and refresh tokens")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest servletRequest) {
        rateLimiterService.checkAllowed("auth:login:" + clientIp(servletRequest),
                loginLimit, Duration.ofSeconds(rateLimitWindowSeconds));
        JwtResponse response = authService.login(request, clientIp(servletRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue a new access token")
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                               HttpServletRequest servletRequest) {
        JwtResponse response = authService.refreshToken(request, clientIp(servletRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout by blacklisting the access token and revoking the refresh token")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestBody(required = false) LogoutRequest request) {
        authService.logout(authorizationHeader, request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
