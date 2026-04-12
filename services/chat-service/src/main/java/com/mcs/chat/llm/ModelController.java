package com.mcs.chat.llm;

import com.mcs.chat.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final Map<String, LlmProvider> providers;

    @GetMapping
    public ApiResponse<Map<String, List<String>>> availableModels() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        providers.forEach((name, provider) -> result.put(name, provider.getAvailableModels()));
        return ApiResponse.ok(result);
    }
}
