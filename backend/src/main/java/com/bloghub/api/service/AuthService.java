package com.bloghub.api.service;

import com.bloghub.api.dto.JwtResponse;
import com.bloghub.api.dto.LoginRequest;
import com.bloghub.api.dto.LogoutRequest;
import com.bloghub.api.dto.RefreshTokenRequest;
import com.bloghub.api.dto.RegisterRequest;
import com.bloghub.api.dto.UserDto;

/**
 * Contract for authentication operations.
 */
public interface AuthService {

    /** Register a new user and return their profile */
    UserDto register(RegisterRequest request);

    /** Authenticate credentials and return a JWT token */
    JwtResponse login(LoginRequest request);

    /** Authenticate credentials and return an access + refresh token pair */
    JwtResponse login(LoginRequest request, String clientIp);

    /** Rotate a refresh token and issue a new access token */
    JwtResponse refreshToken(RefreshTokenRequest request, String clientIp);

    /** Revoke the current access token and optional refresh token */
    void logout(String authorizationHeader, LogoutRequest request);
}
