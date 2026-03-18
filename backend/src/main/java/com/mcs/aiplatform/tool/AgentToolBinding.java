package com.mcs.aiplatform.tool;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "agent_tool_bindings")
@CompoundIndex(name = "agent_tool_idx", def = "{'agentId': 1, 'toolType': 1}", unique = true)
public class AgentToolBinding extends BaseEntity {
    private String agentId;
    private ToolType toolType;
    private boolean enabled = true;
    private Map<String, String> config;
}