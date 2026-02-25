package com.bloghub.service;

import com.bloghub.domain.RoleName;
import com.bloghub.domain.User;
import com.bloghub.exception.ConflictException;
import com.bloghub.repository.RoleRepository;
import com.bloghub.repository.UserRepository;
import com.bloghub.security.BloghubUserPrincipal;
import com.bloghub.security.JwtService;
import com.bloghub.web.dto.UserDto;
import com.bloghub.web.dto.auth.AuthResponse;
import com.bloghub.web.dto.auth.LoginRequest;
import com.bloghub.web.dto.auth.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository users,
            RoleRepository roles,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        var role = roles.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not configured"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setRole(role);

        User saved = users.save(user);

        BloghubUserPrincipal principal = new BloghubUserPrincipal(
                saved.getId(),
                saved.getEmail(),
                saved.getPasswordHash(),
                saved.getRole().getName()
        );
        String token = jwtService.generateToken(principal);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setUser(toUserDto(saved));
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        BloghubUserPrincipal principal = (BloghubUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        User user = users.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setUser(toUserDto(user));
        return response;
    }

    public UserDto getCurrentUser(BloghubUserPrincipal principal) {
        User user = users.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return toUserDto(user);
    }

    private static UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setRole(user.getRole().getName().name());
        return dto;
    }
}

