package com.mcs.aiplatform.usage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsageLogService {

    private final UsageLogRepository repository;

    public UsageLog record(String userId, String agentId, String sessionId,
                           String provider, String model,
                           int promptTokens, int completionTokens) {
        UsageLog entry = new UsageLog();
        entry.setUserId(userId);
        entry.setAgentId(agentId);
        entry.setSessionId(sessionId);
        entry.setProvider(provider);
        entry.setModel(model);
        entry.setPromptTokens(promptTokens);
        entry.setCompletionTokens(completionTokens);
        entry.setTotalTokens(promptTokens + completionTokens);
        entry.setCost(calculateCost(model, promptTokens, completionTokens));
        return repository.save(entry);
    }

    public List<UsageLog> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public Page<UsageLog> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public UsageStatsResponse statsForUser(String userId) {
        List<UsageLog> logs = repository.findByUserId(userId);
        long totalTokens = logs.stream().mapToLong(UsageLog::getTotalTokens).sum();
        double totalCost = logs.stream().mapToDouble(UsageLog::getCost).sum();

        Map<String, Long> byAgent = logs.stream()
                .filter(l -> l.getAgentId() != null)
                .collect(Collectors.groupingBy(UsageLog::getAgentId,
                        Collectors.summingLong(UsageLog::getTotalTokens)));

        Map<String, Long> byModel = logs.stream()
                .filter(l -> l.getModel() != null)
                .collect(Collectors.groupingBy(UsageLog::getModel,
                        Collectors.summingLong(UsageLog::getTotalTokens)));

        Map<String, Double> costByModel = logs.stream()
                .filter(l -> l.getModel() != null)
                .collect(Collectors.groupingBy(UsageLog::getModel,
                        Collectors.summingDouble(UsageLog::getCost)));

        return new UsageStatsResponse(logs.size(), totalTokens, totalCost, byAgent, byModel, costByModel);
    }

    // Rough OpenAI pricing per 1M tokens [input, output]
    private static final Map<String, double[]> PRICING = Map.of(
            "gpt-4o",       new double[]{5.00, 15.00},
            "gpt-4o-mini",  new double[]{0.15,  0.60},
            "gpt-4.1-mini", new double[]{0.40,  1.60},
            "gpt-3.5-turbo",new double[]{0.50,  1.50}
    );

    public static double calculateCost(String model, int promptTokens, int completionTokens) {
        double[] p = PRICING.getOrDefault(model != null ? model.toLowerCase() : "", new double[]{1.0, 2.0});
        return (promptTokens * p[0] + completionTokens * p[1]) / 1_000_000.0;
    }
}