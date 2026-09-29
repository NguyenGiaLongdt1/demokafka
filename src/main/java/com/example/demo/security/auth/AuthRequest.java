package com.example.demo.security.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username khong duoc de trong")
        String username,

        @NotBlank(message = "Password khong duoc de trong")
        String password
) {}
