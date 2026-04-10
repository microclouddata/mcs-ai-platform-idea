package com.mcs.aiplatform.ratelimit;

import com.mcs.aiplatform.billing.PlanType;
import com.mcs.aiplatform.billing.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * D. 网关缓存/限流 — Distributed rate limiter backed by Redis.
 *
 * Replaces the previous in-memory ConcurrentHashMap, which only worked correctly
 * when a single backend replica was running. With Redis INCR + TTL, all replicas
 * share the same counter, making limits accurate under Kubernetes horizontal scaling.
 *
 * Key format : rate-limit:{userId}:{yyyy-MM-dd}   (UTC date)
 * Algorithm  : Redis INCR (atomic counter) — no race conditions.
 * TTL        : 25 hours — slightly longer than a calendar day to handle
 *              requests that arrive just before midnight UTC.
 *
 * Resume/portfolio talking point:
 * "Migrated per-user rate limiting from in-memory HashMap to Redis INCR,
 *  enabling correct enforcement across multiple backend replicas."
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String KEY_PREFIX = "rate-limit:";
    // 25 h covers the full UTC day plus a small midnight rollover buffer
    private static final Duration WINDOW_TTL = Duration.ofHours(25);

    private final SubscriptionService subscriptionService;
    private final StringRedisTemplate redisTemplate;

    public boolean isAllowed(String userId) {
        PlanType plan = subscriptionService.getPlan(userId);
        String key = buildKey(userId);

        // INCR is atomic — safe under concurrent requests from multiple pods
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) return false;

        // Set TTL only on the first increment to avoid resetting it on every call
        if (count == 1) {
            redisTemplate.expire(key, WINDOW_TTL);
        }

        return count <= plan.maxRequestsPerDay;
    }

    public int getRemainingRequests(String userId) {
        PlanType plan = subscriptionService.getPlan(userId);
        String value = redisTemplate.opsForValue().get(buildKey(userId));
        if (value == null) return plan.maxRequestsPerDay;
        return (int) Math.max(0, plan.maxRequestsPerDay - Long.parseLong(value));
    }

    /** Key includes UTC date so the counter automatically resets each day. */
    private String buildKey(String userId) {
        return KEY_PREFIX + userId + ":" + LocalDate.now(ZoneOffset.UTC);
    }
}
