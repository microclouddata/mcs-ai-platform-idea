package com.mcs.aiplatform.integration;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "integrations")
public class Integration extends BaseEntity {
    private String userId;
    private String orgId;
    private IntegrationType type;
    private String name;
    private Map<String, String> config; // webhookUrl, botToken, email, etc.
    private boolean enabled = true;
    private Instant lastTriggeredAt;
}
