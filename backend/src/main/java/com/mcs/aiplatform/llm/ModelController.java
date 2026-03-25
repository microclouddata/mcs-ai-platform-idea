package com.mcs.aiplatform.llm;

import com.mcs.aiplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final Map<String, LlmProvider> providers;

    public ModelController(Map<String, LlmProvider> providers) {
        this.providers = providers;
    }

    @GetMapping
    public ApiResponse<Map<String, List<String>>> list() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        providers.forEach((name, provider) -> result.put(name, provider.getAvailableModels()));
        return ApiResponse.ok(result);
    }
}
