package com.example.demo.messaging.dto;

import java.time.Instant;

public record OrderRequestMessage(
        String requestId,
        Long orderId,
        String orderCode,
        String action,
        String callbackTopic,
        Instant createdAt
) {
}