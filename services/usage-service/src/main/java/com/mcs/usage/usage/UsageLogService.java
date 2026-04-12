package com.mcs.usage.usage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageLogService {

    private final UsageLogRepository usageLogRepository;

    /**
     * Record a usage event to MongoDB.
     */
    public UsageLog record(String userId, String agentId, String sessionId,
                           String provider, String model,
                           int promptTokens, int completionTokens) {
        int totalTokens = promptTokens + completionTokens;
        double cost = calculateCost(model, promptTokens, completionTokens);

        UsageLog log = new UsageLog();
        log.setUserId(userId);
        log.setAgentId(agentId);
        log.setSessionId(sessionId);
        log.setProvider(provider);
        log.setModel(model);
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setTotalTokens(totalTokens);
        log.setCost(cost);

        return usageLogRepository.save(log);
    }

    /**
     * Return all usage logs for a given user.
     */
    public List<UsageLog> getByUser(String userId) {
        return usageLogRepository.findByUserId(userId);
    }

    /**
     * Compute aggregated stats for a user.
     */
    public UsageStatsResponse statsForUser(String userId) {
        List<UsageLog> logs = usageLogRepository.findByUserId(userId);

        long totalRequests = logs.size();
        long totalTokens = logs.stream().mapToLong(UsageLog::getTotalTokens).sum();
        double totalCost = logs.stream().mapToDouble(UsageLog::getCost).sum();

        Map<String, Long> tokensByAgent = logs.stream()
                .filter(l -> l.getAgentId() != null)
                .collect(Collectors.groupingBy(
                        UsageLog::getAgentId,
                        Collectors.summingLong(UsageLog::getTotalTokens)
                ));

        Map<String, Long> tokensByModel = logs.stream()
                .filter(l -> l.getModel() != null)
                .collect(Collectors.groupingBy(
                        UsageLog::getModel,
                        Collectors.summingLong(UsageLog::getTotalTokens)
                ));

        Map<String, Double> costByModel = logs.stream()
                .filter(l -> l.getModel() != null)
                .collect(Collectors.groupingBy(
                        UsageLog::getModel,
                        Collectors.summingDouble(UsageLog::getCost)
                ));

        return new UsageStatsResponse(totalRequests, totalTokens, totalCost,
                tokensByAgent, tokensByModel, costByModel);
    }

    /**
     * Rough cost calculation based on known model pricing (per 1M tokens).
     * Prompt and completion prices sourced from OpenAI pricing approximations.
     */
    public double calculateCost(String model, int promptTokens, int completionTokens) {
        if (model == null) {
            return 0.0;
        }
        String lowerModel = model.toLowerCase();

        double promptPricePerMillion;
        double completionPricePerMillion;

        if (lowerModel.contains("gpt-4o-mini")) {
            promptPricePerMillion = 0.15;
            completionPricePerMillion = 0.60;
        } else if (lowerModel.contains("gpt-4o")) {
            promptPricePerMillion = 2.50;
            completionPricePerMillion = 10.00;
        } else if (lowerModel.contains("gpt-4-turbo") || lowerModel.contains("gpt-4-1106")) {
            promptPricePerMillion = 10.00;
            completionPricePerMillion = 30.00;
        } else if (lowerModel.contains("gpt-4")) {
            promptPricePerMillion = 30.00;
            completionPricePerMillion = 60.00;
        } else if (lowerModel.contains("gpt-3.5-turbo")) {
            promptPricePerMillion = 0.50;
            completionPricePerMillion = 1.50;
        } else if (lowerModel.contains("claude-3-5-sonnet") || lowerModel.contains("claude-sonnet")) {
            promptPricePerMillion = 3.00;
            completionPricePerMillion = 15.00;
        } else if (lowerModel.contains("claude-3-haiku") || lowerModel.contains("claude-haiku")) {
            promptPricePerMillion = 0.25;
            completionPricePerMillion = 1.25;
        } else if (lowerModel.contains("claude-3-opus") || lowerModel.contains("claude-opus")) {
            promptPricePerMillion = 15.00;
            completionPricePerMillion = 75.00;
        } else {
            // Default: treat as gpt-3.5-turbo pricing for unknown models
            promptPricePerMillion = 0.50;
            completionPricePerMillion = 1.50;
        }

        double promptCost = (promptTokens / 1_000_000.0) * promptPricePerMillion;
        double completionCost = (completionTokens / 1_000_000.0) * completionPricePerMillion;
        return promptCost + completionCost;
    }
}
