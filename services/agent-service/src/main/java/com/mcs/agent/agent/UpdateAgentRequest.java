package com.mcs.agent.agent;

import java.util.List;

public record UpdateAgentRequest(
        String name,
        String description,
        String systemPrompt,
        String provider,
        String model,
        Double temperature,
        List<String> tools,
        Boolean memoryEnabled,
        Boolean toolsEnabled
) {}
