package com.example.demo.security.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username khong duoc de trong")
        @Size(min = 3, max = 50, message = "Username phai tu 3 den 50 ky tu")
        String username,

        @NotBlank(message = "Password khong duoc de trong")
        @Size(min = 6, message = "Password phai co it nhat 6 ky tu")
        String password,

        @NotBlank(message = "Email khong duoc de trong")
        @Email(message = "Email khong dung dinh dang")
        String email,

        String phone
) {}
