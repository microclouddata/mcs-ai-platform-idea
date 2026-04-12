package com.mcs.agent.agent;

import com.mcs.agent.common.BaseEntity;
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
    private String model = "gpt-3.5-turbo";
    private Double temperature = 0.2;
    private List<String> tools = new ArrayList<>();
    private boolean enabled = true;
    private boolean memoryEnabled = false;
    private boolean toolsEnabled = true;
}
