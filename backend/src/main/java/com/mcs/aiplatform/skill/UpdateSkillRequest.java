package com.mcs.aiplatform.skill;

import java.util.List;
import java.util.Map;

public record UpdateSkillRequest(
        String name,
        String description,
        String license,
        String compatibility,
        Map<String, String> skillMetadata,
        List<String> allowedTools,
        String instructions,
        SkillStatus status,
        Boolean modelTool
) {
}
