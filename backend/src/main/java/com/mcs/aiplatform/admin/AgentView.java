package com.mcs.aiplatform.admin;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/** Read-only view of the agents collection owned by agent-service. */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "agents")
public class AgentView extends BaseEntity {
    private String userId;
    private String name;
    private String description;
    private String provider;
    private String model;
    private boolean memoryEnabled;
    private boolean toolsEnabled;
    private boolean enabled;
}
