package com.mcs.aiplatform.apikey;
import java.time.Instant;
import java.util.List;

public record ApiKeyResponse(
        String id,
        String name,
        String keyPrefix,
        String plainKey,   // only present on creation, null afterwards
        List<String> scopes,
        boolean enabled,
        Instant lastUsedAt,
        Instant expiresAt,
        Instant createdAt
) {}
