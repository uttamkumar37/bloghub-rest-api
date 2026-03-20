package com.bloghub.api.service;

import com.bloghub.api.dto.JwtResponse;
import com.bloghub.api.dto.LoginRequest;
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
}
