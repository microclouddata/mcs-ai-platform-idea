package com.mcs.chat.kafka.producer;

import com.mcs.chat.kafka.KafkaTopics;
import com.mcs.chat.kafka.event.UsageRecordedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

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
