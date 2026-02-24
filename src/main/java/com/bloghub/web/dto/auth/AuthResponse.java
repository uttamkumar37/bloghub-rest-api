package com.bloghub.web.dto.auth;

import com.bloghub.web.dto.UserDto;

public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDto user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}

