package com.example.demo.order.dto;

import com.example.demo.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderCode,
        String customerName,
        BigDecimal amount,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}