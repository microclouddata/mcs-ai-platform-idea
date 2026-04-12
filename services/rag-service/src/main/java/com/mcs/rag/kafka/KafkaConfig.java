package com.mcs.rag.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_UPLOADED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
