package com.mcs.aiplatform.kafka.producer;

import com.mcs.aiplatform.kafka.KafkaTopics;
import com.mcs.aiplatform.kafka.event.UsageRecordedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a {@code usage.recorded} event keyed by {@code userId} so all usage
     * events for the same user land on the same partition (ordering guarantee).
     */
    public void publishUsageRecorded(UsageRecordedEvent event) {
        kafkaTemplate.send(KafkaTopics.USAGE_RECORDED, event.userId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] Failed to publish usage.recorded for userId={}: {}",
                                event.userId(), ex.getMessage());
                    } else {
                        log.debug("[Kafka] Published usage.recorded: userId={}", event.userId());
                    }
                });
    }
}
