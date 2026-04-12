package com.mcs.chat.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic usageRecordedTopic() {
        return TopicBuilder.name(KafkaTopics.USAGE_RECORDED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
