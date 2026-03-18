package com.mcs.aiplatform.usage;

import java.util.Map;

public record UsageStatsResponse(
        long totalRequests,
        long totalTokens,
        double totalCost,
        Map<String, Long> tokensByAgent,
        Map<String, Long> tokensByModel,
        Map<String, Double> costByModel
) {}
