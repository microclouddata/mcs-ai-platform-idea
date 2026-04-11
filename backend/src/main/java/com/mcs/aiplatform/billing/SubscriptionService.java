package com.mcs.aiplatform.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * Upgrades the plan and evicts the user-plans cache so the new plan takes
     * effect immediately in RateLimitService on the next request.
     */
    @CacheEvict(value = "user-plans", key = "#userId")
    public Subscription upgrade(String userId, PlanType plan) {
        Subscription sub = getOrCreate(userId);
        sub.setPlan(plan);
        sub.setStatus("ACTIVE");
        sub.setPeriodStart(Instant.now());
        sub.setPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
        return subscriptionRepository.save(sub);
    }

    /**
     * A. 热配置缓存 — Plan type is looked up on every rate-limit check.
     * Cache per userId with 30-min TTL. Evicted on plan upgrade.
     */
    @Cacheable(value = "user-plans", key = "#userId")
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
