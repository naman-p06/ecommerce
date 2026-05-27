package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private final String token;
    private final String type = "Bearer";
}