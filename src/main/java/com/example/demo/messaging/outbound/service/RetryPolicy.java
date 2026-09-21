package com.example.demo.messaging.outbound.service;

import com.example.demo.config.OutboundProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RetryPolicy {

    private final OutboundProperties properties;

    public boolean canRetry(int currentRetryCount) {
        return currentRetryCount < properties.maxRetry();
    }

    public LocalDateTime nextRetryAt(int currentRetryCount) {
        List<Long> delays = properties.retryDelaysSeconds();
        int index = Math.min(currentRetryCount, delays.size() - 1);
        return LocalDateTime.now().plusSeconds(delays.get(index));
    }
}
