package com.mcs.agent.skill;

import com.mcs.agent.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "skills")
@CompoundIndex(name = "skill_agent_idx", def = "{'agentId': 1, 'createdAt': -1}")
public class Skill extends BaseEntity {
    private String agentId;
    private String name;
    private String description;
    private String license;
    private String compatibility;
    private Map<String, String> skillMetadata = new LinkedHashMap<>();
    private List<String> allowedTools = new ArrayList<>();
    private String instructions;
    private SkillStatus status = SkillStatus.ACTIVE;
    private boolean modelTool = false;
}
