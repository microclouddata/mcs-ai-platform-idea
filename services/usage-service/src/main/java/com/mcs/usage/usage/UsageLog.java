package com.mcs.usage.usage;

import com.mcs.usage.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "usage_logs")
public class UsageLog extends BaseEntity {

    @Indexed
    private String userId;

    @Indexed
    private String agentId;

    private String sessionId;

    private String provider;

    private String model;

    private int promptTokens;

    private int completionTokens;

    private int totalTokens;

    private double cost;
}
