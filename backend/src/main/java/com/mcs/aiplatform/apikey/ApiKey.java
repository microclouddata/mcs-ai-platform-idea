package com.mcs.aiplatform.apikey;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "api_keys")
public class ApiKey extends BaseEntity {
    private String userId;
    private String orgId;
    private String name;
    @Indexed(unique = true)
    private String keyHash;      // SHA-256 of the actual key
    private String keyPrefix;    // First 8 chars for display
    private List<String> scopes; // e.g. ["chat", "agents", "workflows"]
    private boolean enabled = true;
    private Instant lastUsedAt;
    private Instant expiresAt;
}
