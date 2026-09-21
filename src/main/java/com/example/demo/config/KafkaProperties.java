package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        String requestTopic,
        String resultTopic,
        String deadLetterTopic
) {
}