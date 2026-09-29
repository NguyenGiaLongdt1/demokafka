package com.example.demo.security.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String username,
        String role,
        long expiresInMs
) {
    public AuthResponse(String accessToken, String refreshToken, String username, String role, long expiresInMs) {
        this(accessToken, refreshToken, "Bearer", username, role, expiresInMs);
    }
}

