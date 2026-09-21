package com.example.demo.messaging.inbound;

import com.example.demo.messaging.dto.OrderResultMessage;
import com.example.demo.messaging.idempotency.ProcessedMessageEntity;
import com.example.demo.messaging.idempotency.ProcessedMessageRepository;
import com.example.demo.order.entity.OrderStatus;
import com.example.demo.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderResultListener {

    private static final String CONSUMER_NAME = "order-result-listener";

    private final OrderRepository orderRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    @KafkaListener(
            topics = "${app.kafka.result-topic}",
            properties = {"spring.json.value.default.type=com.example.demo.messaging.dto.OrderResultMessage"})
    @Transactional
    public void onOrderResult(OrderResultMessage message) {
        log.info("[RESULT_RECEIVED] requestId={} orderId={} success={}",
                message.requestId(), message.orderId(), message.success());

        if (processedMessageRepository
                .existsByMessageIdAndConsumerName(message.requestId(), CONSUMER_NAME)) {
            log.info("[RESULT_DUPLICATE_IGNORED] requestId={} - da xu ly roi, bo qua",
                    message.requestId());
            return;
        }

        orderRepository.findById(message.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() != OrderStatus.PENDING_SEND
                    && order.getStatus() != OrderStatus.SENT) {
                log.warn("[RESULT_UNEXPECTED_STATUS] requestId={} currentStatus={} - bo qua",
                        message.requestId(), order.getStatus());
                return;
            }
            order.setStatus(message.success() ? OrderStatus.APPROVED : OrderStatus.REJECTED);
            log.info("[ORDER_{}] requestId={} orderId={}",
                    message.success() ? "APPROVED" : "REJECTED",
                    message.requestId(), message.orderId());
        }, () -> log.error("[RESULT_ORDER_NOT_FOUND] requestId={} orderId={}",
                message.requestId(), message.orderId()));

        ProcessedMessageEntity marker = new ProcessedMessageEntity();
        marker.setMessageId(message.requestId());
        marker.setConsumerName(CONSUMER_NAME);
        processedMessageRepository.save(marker);
    }
}
