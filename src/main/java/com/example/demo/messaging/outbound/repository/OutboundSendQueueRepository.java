package com.example.demo.messaging.outbound.repository;

import com.example.demo.messaging.outbound.entity.OutboundSendQueueEntity;
import com.example.demo.messaging.outbound.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboundSendQueueRepository
        extends JpaRepository<OutboundSendQueueEntity, Long> {

    @Query(value = """
            SELECT id FROM outbound_send_queue
            WHERE (status = 'PENDING')
               OR (status = 'RETRY' AND next_retry_at <= :now)
            ORDER BY id ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> claimReadyIds(@Param("now") LocalDateTime now, @Param("limit") int limit);

    List<OutboundSendQueueEntity> findByStatusAndUpdatedAtBefore(
            QueueStatus status, LocalDateTime before);
}
