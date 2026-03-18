package com.mcs.aiplatform.ratelimit;

import com.mcs.aiplatform.billing.PlanType;
import com.mcs.aiplatform.billing.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final SubscriptionService subscriptionService;

    // key: userId, value: [count, windowStartEpochSecond]
    private final ConcurrentHashMap<String, long[]> windows = new ConcurrentHashMap<>();

    public boolean isAllowed(String userId) {
        PlanType plan = subscriptionService.getPlan(userId);
        int limit = plan.maxRequestsPerDay;

        long nowEpoch = Instant.now().truncatedTo(ChronoUnit.DAYS).getEpochSecond();
        long[] window = windows.compute(userId, (k, existing) -> {
            if (existing == null || existing[1] != nowEpoch) {
                return new long[]{1, nowEpoch};
            }
            existing[0]++;
            return existing;
        });
        return window[0] <= limit;
    }

    public int getRemainingRequests(String userId) {
        PlanType plan = subscriptionService.getPlan(userId);
        long nowEpoch = Instant.now().truncatedTo(ChronoUnit.DAYS).getEpochSecond();
        long[] window = windows.get(userId);
        if (window == null || window[1] != nowEpoch) return plan.maxRequestsPerDay;
        return (int) Math.max(0, plan.maxRequestsPerDay - window[0]);
    }
}
