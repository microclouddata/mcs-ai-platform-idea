package com.mcs.agent.agent;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateAgentRequest(
        @NotBlank String name,
        String description,
        String systemPrompt,
        String provider,
        String model,
        Double temperature,
        List<String> tools,
        Boolean memoryEnabled,
        Boolean toolsEnabled
) {}
