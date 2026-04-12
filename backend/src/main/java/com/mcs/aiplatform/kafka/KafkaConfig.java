package com.mcs.aiplatform.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates Kafka topics on startup if they do not already exist.
 * Partitions = 3 to allow parallel consumer instances under K8s HPA.
 * Replicas  = 1 (single-broker dev/demo cluster; set to 3 in production).
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_UPLOADED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic usageRecordedTopic() {
        return TopicBuilder.name(KafkaTopics.USAGE_RECORDED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
