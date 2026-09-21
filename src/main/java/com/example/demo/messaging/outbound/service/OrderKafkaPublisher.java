package com.example.demo.messaging.outbound.service;

import com.example.demo.messaging.dto.OrderRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public String publish(String topic, OrderRequestMessage message) {
        log.info("[KAFKA_PUBLISHING] requestId={} orderCode={} topic={}",
                message.requestId(), message.orderCode(), topic);
        try {
            SendResult<String, Object> result = kafkaTemplate
                    .send(topic, message.orderCode(), message)
                    .get(10, TimeUnit.SECONDS);
            String providerMessageId = topic
                    + "-" + result.getRecordMetadata().partition()
                    + "-" + result.getRecordMetadata().offset();
            log.info("[KAFKA_PUBLISHED] requestId={} topic={} partition={} offset={}",
                    message.requestId(), topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return providerMessageId;
        } catch (Exception e) {
            log.error("[KAFKA_PUBLISH_FAILED] requestId={} topic={}",
                    message.requestId(), topic, e);
            throw new IllegalStateException("Gui Kafka that bai requestId=" + message.requestId(), e);
        }
    }
}
