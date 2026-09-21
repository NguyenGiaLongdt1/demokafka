package com.example.demo.messaging.outbound.worker;

import com.example.demo.config.OutboundProperties;
import com.example.demo.messaging.outbound.api.OutboundGateway;
import com.example.demo.messaging.outbound.entity.OutboundSendQueueEntity;
import com.example.demo.messaging.outbound.entity.QueueStatus;
import com.example.demo.messaging.outbound.repository.OutboundSendQueueRepository;
import com.example.demo.messaging.outbound.service.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.outbound", name = "worker-enabled", havingValue = "true")
public class OutboundWorker {

    private final OutboundSendQueueRepository queueRepository;
    private final OutboundGateway outboundGateway;
    private final OutboundProperties outboundProperties;
    private final RetryPolicy retryPolicy;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelay = 2000)
    public void processQueue() {
        recoverStuckProcessing();

        List<OutboundSendQueueEntity> batch = transactionTemplate.execute(status -> claimReadyBatch());
        if (batch == null || batch.isEmpty()) {
            return;
        }
        for (OutboundSendQueueEntity row : batch) {
            processClaimed(row);
        }
    }

    private List<OutboundSendQueueEntity> claimReadyBatch() {
        List<Long> ids = queueRepository.claimReadyIds(
                LocalDateTime.now(), outboundProperties.batchSize());
        List<OutboundSendQueueEntity> rows = new ArrayList<>();
        for (Long id : ids) {
            queueRepository.findById(id).ifPresent(row -> {
                row.setStatus(QueueStatus.PROCESSING);
                rows.add(row);
            });
        }
        return rows;
    }

    private void recoverStuckProcessing() {
        LocalDateTime timeout = LocalDateTime.now()
                .minusSeconds(outboundProperties.processingTimeoutSeconds());
        List<OutboundSendQueueEntity> stuck = queueRepository.findByStatusAndUpdatedAtBefore(
                QueueStatus.PROCESSING, timeout);
        for (OutboundSendQueueEntity row : stuck) {
            transactionTemplate.executeWithoutResult(status -> {
                OutboundSendQueueEntity fresh = queueRepository.findById(row.getId()).orElseThrow();
                if (fresh.getStatus() == QueueStatus.PROCESSING) {
                    fresh.setStatus(QueueStatus.RETRY);
                    fresh.setNextRetryAt(LocalDateTime.now());
                    log.warn("[OUTBOUND_STUCK_RECOVERED] requestId={} queueId={} - PROCESSING qua timeout",
                            fresh.getRequestId(), fresh.getId());
                }
            });
        }
    }

    private void processClaimed(OutboundSendQueueEntity row) {
        log.info("[OUTBOUND_CLAIMED] requestId={} queueId={} retryCount={}",
                row.getRequestId(), row.getId(), row.getRetryCount());

        try {
            String providerMessageId = outboundGateway.send(row);
            transactionTemplate.executeWithoutResult(status ->
                    markSent(row.getId(), providerMessageId));
            log.info("[OUTBOUND_SENT] requestId={} queueId={} providerMessageId={}",
                    row.getRequestId(), row.getId(), providerMessageId);
        } catch (Exception e) {
            log.error("[OUTBOUND_SEND_FAILED] requestId={} queueId={} retryCount={}",
                    row.getRequestId(), row.getId(), row.getRetryCount(), e);
            transactionTemplate.executeWithoutResult(status ->
                    markFailedAttempt(row.getId(), e));
        }
    }

    private void markSent(Long queueId, String providerMessageId) {
        OutboundSendQueueEntity row = queueRepository.findById(queueId).orElseThrow();
        row.setStatus(QueueStatus.SENT);
        row.setProviderMessageId(providerMessageId);
        row.setErrorCode(null);
        row.setErrorMessage(null);
        row.setNextRetryAt(null);
    }

    private void markFailedAttempt(Long queueId, Exception e) {
        OutboundSendQueueEntity row = queueRepository.findById(queueId).orElseThrow();
        int nextRetryCount = row.getRetryCount() + 1;
        row.setRetryCount(nextRetryCount);
        row.setErrorCode("KAFKA_ERROR");
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (message.length() > 2000) {
            message = message.substring(0, 2000);
        }
        row.setErrorMessage(message);
        if (retryPolicy.canRetry(row.getRetryCount())) {
            row.setStatus(QueueStatus.RETRY);
            row.setNextRetryAt(retryPolicy.nextRetryAt(row.getRetryCount()));
            log.info("[OUTBOUND_RETRY_SCHEDULED] requestId={} queueId={} retryCount={} nextRetryAt={}",
                    row.getRequestId(), queueId, nextRetryCount, row.getNextRetryAt());
        } else {
            row.setStatus(QueueStatus.FAILED);
            row.setNextRetryAt(null);
            log.error("[OUTBOUND_FAILED] requestId={} queueId={} retryCount={} - het luot retry",
                    row.getRequestId(), queueId, nextRetryCount);
        }
    }
}
