package com.mcs.usage.kafka.consumer;

import com.mcs.usage.kafka.KafkaTopics;
import com.mcs.usage.kafka.event.UsageRecordedEvent;
import com.mcs.usage.usage.UsageLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageConsumer {

    private final UsageLogService usageLogService;

    @KafkaListener(topics = KafkaTopics.USAGE_RECORDED, groupId = "usage-recorder")
    public void consume(UsageRecordedEvent event) {
        try {
            usageLogService.record(
                    event.userId(),
                    event.agentId(),
                    event.sessionId(),
                    event.provider(),
                    event.model(),
                    event.promptTokens(),
                    event.completionTokens()
            );
            log.info("Recorded usage for userId={} agentId={} model={} promptTokens={} completionTokens={}",
                    event.userId(), event.agentId(), event.model(),
                    event.promptTokens(), event.completionTokens());
        } catch (Exception e) {
            log.error("Failed to record usage event for userId={} agentId={}: {}",
                    event.userId(), event.agentId(), e.getMessage(), e);
            // Re-throw to trigger Kafka retry / dead-letter handling
            throw new RuntimeException("Failed to process UsageRecordedEvent", e);
        }
    }
}
