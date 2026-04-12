package com.mcs.aiplatform.kafka.consumer;

import com.mcs.aiplatform.kafka.KafkaTopics;
import com.mcs.aiplatform.kafka.event.UsageRecordedEvent;
import com.mcs.aiplatform.usage.UsageLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Async usage persistence consumer.
 *
 * <p>Decouples MongoDB writes from the critical chat request path. A transient
 * database failure here no longer affects the user's chat experience — Kafka
 * will retry delivery according to the consumer group offset.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageConsumer {

    private final UsageLogService usageLogService;

    @KafkaListener(
            topics = KafkaTopics.USAGE_RECORDED,
            groupId = "usage-recorder",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void recordUsage(UsageRecordedEvent event) {
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
            log.debug("[Kafka] Usage recorded: userId={}, model={}, tokens={}+{}",
                    event.userId(), event.model(), event.promptTokens(), event.completionTokens());
        } catch (Exception e) {
            log.error("[Kafka] Failed to persist usage log for userId={}: {}", event.userId(), e.getMessage(), e);
            // Re-throw so Kafka retries based on the error-handler / retry config
            throw new RuntimeException("Usage persistence failed", e);
        }
    }
}
