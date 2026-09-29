package com.example.demo.security.auth;

import java.time.Instant;

public record UserProfileResponse(
        String username,
        String emailMasked,
        String phoneMasked,
        String role,
        Instant createdAt
) {}
