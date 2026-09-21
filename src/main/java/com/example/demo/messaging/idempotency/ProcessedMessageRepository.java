package com.example.demo.messaging.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository
        extends JpaRepository<ProcessedMessageEntity, Long> {

    boolean existsByMessageIdAndConsumerName(String messageId, String consumerName);
}
