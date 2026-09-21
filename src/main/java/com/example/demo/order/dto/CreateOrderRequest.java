package com.example.demo.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank(message = "Ten khach hang khong duoc trong")
        @Size(max = 255)
        String customerName,

        @NotNull(message = "So tien khong duoc trong")
        @Positive(message = "So tien phai lon hon 0")
        BigDecimal amount
) {
}