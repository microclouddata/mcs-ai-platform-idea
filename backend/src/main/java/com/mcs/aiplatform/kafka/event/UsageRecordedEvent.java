package com.mcs.aiplatform.kafka.event;

/**
 * Emitted by {@code ChatService} after every LLM response instead of calling
 * {@code UsageLogService.record()} inline.
 *
 * <p>Decouples usage persistence from the critical chat request path — a transient
 * MongoDB write failure will no longer affect the user's chat experience.
 */
public record UsageRecordedEvent(
        String userId,
        String agentId,
        String sessionId,
        String provider,
        String model,
        int promptTokens,
        int completionTokens
) {}
