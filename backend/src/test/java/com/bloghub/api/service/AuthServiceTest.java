package com.bloghub.api.service;

import com.bloghub.api.dto.JwtResponse;
import com.bloghub.api.dto.LoginRequest;
import com.bloghub.api.dto.RegisterRequest;
import com.bloghub.api.dto.UserDto;
import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.repository.RoleRepository;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.security.JwtTokenProvider;
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
        registerRequest.setPassword("password123");

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
        given(passwordEncoder.encode("password123")).willReturn("hashed");
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
        loginRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(tokenProvider.generateToken(authentication)).willReturn("mocked.jwt.token");
        given(userRepository.findByUsernameOrEmail("alice", "alice")).willReturn(Optional.of(savedUser));
        given(authentication.getAuthorities()).willReturn(java.util.Collections.emptyList());

        JwtResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }
}
