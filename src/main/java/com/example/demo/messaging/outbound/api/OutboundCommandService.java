package com.example.demo.messaging.outbound.api;

public interface OutboundCommandService {

    void enqueueOrderSubmit(Long orderId, String orderCode, String requestId);
}
