package com.bloghub.web;

import com.bloghub.security.BloghubUserPrincipal;
import com.bloghub.security.CurrentUser;
import com.bloghub.service.AuthService;
import com.bloghub.web.dto.ApiResponse;
import com.bloghub.web.dto.UserDto;
import com.bloghub.web.dto.auth.AuthResponse;
import com.bloghub.web.dto.auth.LoginRequest;
import com.bloghub.web.dto.auth.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(@CurrentUser BloghubUserPrincipal principal) {
        UserDto dto = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}

