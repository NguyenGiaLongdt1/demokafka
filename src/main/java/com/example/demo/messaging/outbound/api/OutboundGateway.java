package com.example.demo.messaging.outbound.api;

import com.example.demo.messaging.outbound.entity.OutboundSendQueueEntity;

public interface OutboundGateway {

    String send(OutboundSendQueueEntity queue);
}
