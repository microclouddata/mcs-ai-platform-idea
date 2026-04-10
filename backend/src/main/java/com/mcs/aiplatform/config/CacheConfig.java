package com.mcs.aiplatform.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Redis caching strategy — four cache tiers as per architecture design:
 *
 * A. 热配置缓存 (Hot-config cache, TTL 30 min)
 *    "agents"      — Agent configs looked up on every chat request.
 *    "agent-tools" — Tool bindings per agent, rarely change.
 *    "agent-skills"— Skill list per agent, rarely change.
 *    "user-plans"  — Subscription plan per user, used in rate limiting.
 *
 * C. 检索结果缓存 (Retrieval cache, TTL 60 min)
 *    "embeddings"  — OpenAI embedding vectors; identical text always yields
 *                    the same vector, so caching avoids expensive API calls.
 *
 * D. 网关缓存/限流 (Distributed rate limiting)
 *    Implemented directly in RateLimitService via StringRedisTemplate INCR.
 *    Not managed here because it uses custom key/TTL logic per user per day.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // A. Hot-config: agent metadata, tool bindings, skills, subscription plans
    private static final Duration HOT_CONFIG_TTL  = Duration.ofMinutes(30);

    // C. Retrieval: embedding vectors from OpenAI (expensive, deterministic)
    private static final Duration RETRIEVAL_TTL   = Duration.ofHours(1);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Use JSON serialization so cache entries are human-readable in Redis CLI / RedisInsight.
        // GenericJackson2JsonRedisSerializer embeds the Java type in the JSON (@class field),
        // enabling correct deserialization of complex domain objects.
        var jsonSerializer = new GenericJackson2JsonRedisSerializer();
        var serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer);

        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializationPair)
                .disableCachingNullValues();  // Don't cache null (e.g. "not found") results

        Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
                // A. Hot-config cache
                "agents",       defaultConfig.entryTtl(HOT_CONFIG_TTL),
                "agent-tools",  defaultConfig.entryTtl(HOT_CONFIG_TTL),
                "agent-skills", defaultConfig.entryTtl(HOT_CONFIG_TTL),
                "user-plans",   defaultConfig.entryTtl(HOT_CONFIG_TTL),
                // C. Retrieval cache
                "embeddings",   defaultConfig.entryTtl(RETRIEVAL_TTL)
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(HOT_CONFIG_TTL))
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }
}
