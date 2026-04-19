package com.mcs.aiplatform.admin;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/** Read-only view of the usage_logs collection owned by usage-service. */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "usage_logs")
public class UsageLogView extends BaseEntity {
    private String userId;
    private String agentId;
    private String sessionId;
    private String provider;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private Double cost;
}
