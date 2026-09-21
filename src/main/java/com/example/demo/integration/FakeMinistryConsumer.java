package com.example.demo.integration;

import com.example.demo.messaging.dto.OrderRequestMessage;
import com.example.demo.messaging.dto.OrderResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class FakeMinistryConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = "${app.kafka.request-topic}",
            groupId = "fake-ministry",
            properties = {"spring.json.value.default.type=com.example.demo.messaging.dto.OrderRequestMessage"})
    public void onOrderRequest(OrderRequestMessage message) {
        log.info("[FAKE_MINISTRY_RECEIVED] requestId={} orderCode={} action={}",
                message.requestId(), message.orderCode(), message.action());

        boolean approved = isApproved(message.orderCode());
        OrderResultMessage result = new OrderResultMessage(
                message.requestId(),
                message.orderId(),
                approved,
                approved ? null : "MINISTRY_REJECTED",
                approved ? null : "Bo Tu choi duyet don hang nay",
                Instant.now());

        kafkaTemplate.send(message.callbackTopic(), message.orderCode(), result);
        log.info("[FAKE_MINISTRY_REPLIED] requestId={} success={} callbackTopic={}",
                message.requestId(), approved, message.callbackTopic());
    }

    private boolean isApproved(String orderCode) {
        char lastDigit = orderCode.charAt(orderCode.length() - 1);
        return (lastDigit - '0') % 2 == 0;
    }
}