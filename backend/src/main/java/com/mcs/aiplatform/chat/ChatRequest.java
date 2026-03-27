package com.mcs.aiplatform.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String agentId,
        String sessionId,
        @NotBlank String message,
        String skillId
) {}
