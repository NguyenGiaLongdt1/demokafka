package com.example.demo.messaging.outbound.entity;

public enum QueueStatus {
    PENDING,
    PROCESSING,
    SENT,
    RETRY,
    FAILED
}