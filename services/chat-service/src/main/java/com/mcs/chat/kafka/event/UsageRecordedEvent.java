package com.mcs.chat.kafka.event;

public record UsageRecordedEvent(
        String userId,
        String agentId,
        String sessionId,
        String provider,
        String model,
        int promptTokens,
        int completionTokens
) {}
