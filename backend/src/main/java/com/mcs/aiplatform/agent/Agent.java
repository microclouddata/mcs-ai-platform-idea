package com.mcs.aiplatform.agent;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "agents")
@CompoundIndex(def = "{'userId': 1, 'createdAt': -1}")
public class Agent extends BaseEntity {
    private String userId;
    private String organizationId;
    private String name;
    private String description;
    private String systemPrompt;
    private String provider = "OPENAI";
    private String model = "gpt-4.1-mini";
    private Double temperature = 0.2;
    private List<String> tools = new ArrayList<>();

    // Phase 2
    private boolean enabled = true;
    private boolean memoryEnabled = false;
    private boolean toolsEnabled = true;
    private List<String> knowledgeBaseIds = new ArrayList<>();
}
