package com.mcs.aiplatform.skill;

import java.util.List;
import java.util.Map;

public record CreateSkillRequest(
        String name,
        String description,
        String code,
        SkillLanguage language,
        SkillStatus status,
        SkillType skillType,
        String docId,
        List<String> controlFlags,
        Map<String, String> metadata,
        List<String> tags,
        List<SkillParameter> parameters,
        Boolean modelTool
) {
}
