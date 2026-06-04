package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType  = "Bearer";
    private final long   expiresIn;     // access token lifetime in seconds (for frontend countdown)
}