package com.mcs.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
public class CacheConfig {

    private static final Duration HOT_CONFIG_TTL = Duration.ofMinutes(30);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        var jsonSerializer = new GenericJackson2JsonRedisSerializer();
        var serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer);

        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializationPair)
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
                "agents",       defaultConfig.entryTtl(HOT_CONFIG_TTL),
                "agent-tools",  defaultConfig.entryTtl(HOT_CONFIG_TTL),
                "agent-skills", defaultConfig.entryTtl(HOT_CONFIG_TTL)
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(HOT_CONFIG_TTL))
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }
}
