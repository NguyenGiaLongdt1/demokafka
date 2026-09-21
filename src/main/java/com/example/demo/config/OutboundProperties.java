package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.outbound")
public record OutboundProperties(
        boolean workerEnabled,
        int batchSize,
        int maxRetry,
        long processingTimeoutSeconds,
        List<Long> retryDelaysSeconds
) {
}
