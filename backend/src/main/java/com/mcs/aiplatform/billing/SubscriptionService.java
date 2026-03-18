package com.mcs.aiplatform.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Subscription getOrCreate(String userId) {
        return subscriptionRepository.findByUserId(userId).orElseGet(() -> {
            Subscription sub = new Subscription();
            sub.setUserId(userId);
            sub.setPlan(PlanType.FREE);
            sub.setStatus("ACTIVE");
            sub.setPeriodStart(Instant.now());
            sub.setPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
            return subscriptionRepository.save(sub);
        });
    }

    public Subscription upgrade(String userId, PlanType plan) {
        Subscription sub = getOrCreate(userId);
        sub.setPlan(plan);
        sub.setStatus("ACTIVE");
        sub.setPeriodStart(Instant.now());
        sub.setPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
        return subscriptionRepository.save(sub);
    }

    public PlanType getPlan(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(Subscription::getPlan)
                .orElse(PlanType.FREE);
    }

    public boolean isWithinLimit(String userId, LimitType limitType, int currentCount) {
        PlanType plan = getPlan(userId);
        return switch (limitType) {
            case AGENTS -> currentCount < plan.maxAgents;
            case REQUESTS_PER_DAY -> currentCount < plan.maxRequestsPerDay;
            case DOCUMENTS -> currentCount < plan.maxDocuments;
        };
    }
}
