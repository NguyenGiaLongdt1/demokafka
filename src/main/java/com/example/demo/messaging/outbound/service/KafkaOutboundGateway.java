package com.example.demo.messaging.outbound.service;

import com.example.demo.messaging.dto.OrderRequestMessage;
import com.example.demo.messaging.outbound.api.OutboundGateway;
import com.example.demo.messaging.outbound.entity.OutboundSendQueueEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOutboundGateway implements OutboundGateway {

    private final OrderKafkaPublisher publisher;
    private final ObjectMapper objectMapper;

    @Override
    public String send(OutboundSendQueueEntity queue) {
        OrderRequestMessage message = objectMapper.readValue(
                queue.getPayload(), OrderRequestMessage.class);
        return publisher.publish(queue.getRequestTopic(), message);
    }
}
