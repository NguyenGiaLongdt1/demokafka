package com.example.demo.security.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token khong duoc de trong")
        String refreshToken
) {}
