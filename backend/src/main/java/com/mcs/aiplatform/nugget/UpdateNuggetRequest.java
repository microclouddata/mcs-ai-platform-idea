package com.mcs.aiplatform.nugget;

import java.util.List;
import java.util.Map;

public record UpdateNuggetRequest(
        String name,
        String description,
        String code,
        NuggetLanguage language,
        NuggetStatus status,
        NuggetType nuggetType,
        String docId,
        List<String> controlFlags,
        Map<String, String> metadata,
        List<String> tags,
        List<NuggetParameter> parameters,
        Boolean modelTool
) {
}
