package com.bloghub.api.dto;

import lombok.Data;

@Data
public class LogoutRequest {

    private String refreshToken;
}
