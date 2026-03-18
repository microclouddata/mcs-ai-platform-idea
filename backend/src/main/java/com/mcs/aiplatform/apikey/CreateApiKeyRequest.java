package com.mcs.aiplatform.apikey;
import java.time.Instant;
import java.util.List;
public record CreateApiKeyRequest(String name, List<String> scopes, Instant expiresAt) {}
