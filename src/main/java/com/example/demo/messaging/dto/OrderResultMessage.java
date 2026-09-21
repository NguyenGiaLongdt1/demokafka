package com.example.demo.messaging.dto;

import java.time.Instant;

public record OrderResultMessage(
        String requestId,
        Long orderId,
        boolean success,
        String errorCode,
        String errorMessage,
        Instant processedAt
) {
}