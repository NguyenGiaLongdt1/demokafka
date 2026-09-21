package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic requestTopic(KafkaProperties properties) {
        return TopicBuilder.name(properties.requestTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic resultTopic(KafkaProperties properties) {
        return TopicBuilder.name(properties.resultTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic(KafkaProperties properties) {
        return TopicBuilder.name(properties.deadLetterTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}