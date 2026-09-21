package com.example.demo.messaging.outbound.service;

import com.example.demo.config.KafkaProperties;
import com.example.demo.messaging.dto.OrderRequestMessage;
import com.example.demo.messaging.outbound.api.OutboundCommandService;
import com.example.demo.messaging.outbound.entity.OutboundSendQueueEntity;
import com.example.demo.messaging.outbound.entity.QueueStatus;
import com.example.demo.messaging.outbound.repository.OutboundSendQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundCommandServiceImpl implements OutboundCommandService {

    private final OutboundSendQueueRepository queueRepository;
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void enqueueOrderSubmit(Long orderId, String orderCode, String requestId) {
        OrderRequestMessage payload = new OrderRequestMessage(
                requestId, orderId, orderCode, "SUBMIT",
                kafkaProperties.resultTopic(), Instant.now());
        try {
            OutboundSendQueueEntity queue = new OutboundSendQueueEntity();
            queue.setRequestId(requestId);
            queue.setBusinessType("ORDER");
            queue.setBusinessId(orderId);
            queue.setBusinessCode(orderCode);
            queue.setAction("SUBMIT");
            queue.setRequestTopic(kafkaProperties.requestTopic());
            queue.setResultTopic(kafkaProperties.resultTopic());
            queue.setPayload(objectMapper.writeValueAsString(payload));
            queue.setStatus(QueueStatus.PENDING);
            queueRepository.save(queue);
            log.info("[OUTBOUND_ENQUEUED] requestId={} orderId={} orderCode={} status=PENDING",
                    requestId, orderId, orderCode);
        } catch (Exception e) {
            throw new IllegalStateException("Khong tao duoc queue row cho requestId=" + requestId, e);
        }
    }
}
