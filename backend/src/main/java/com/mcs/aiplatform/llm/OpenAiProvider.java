package com.mcs.aiplatform.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("OPENAI")
public class OpenAiProvider implements LlmProvider {

    private final boolean mockEnabled;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public OpenAiProvider(@Value("${app.llm.mock-enabled:true}") boolean mockEnabled,
                          @Value("${app.llm.openai.api-key:}") String apiKey,
                          @Value("${app.llm.openai.model:gpt-4.1-mini}") String model) {
        this.mockEnabled = mockEnabled;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        if (mockEnabled || apiKey == null || apiKey.isBlank()) {
            return new LlmResponse("[Mock LLM Reply] No API key configured. Please set OPENAI_API_KEY.");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "input", List.of(
                        Map.of("role", "system", "content", List.of(Map.of("type", "input_text", "text", request.systemPrompt()))),
                        Map.of("role", "user", "content", List.of(Map.of("type", "input_text", "text", request.userPrompt())))
                )
        );

        Map response = restClient.post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

//        Object outputText = null;
//        if (response != null && response.get("output") != null) {
//            Object outputArray = response.get("output");
//            if (outputArray instanceof List && !((List<?>) outputArray).isEmpty()) {
//                Map firstOutput = (Map) ((List<?>) outputArray).get(0);
//                if (firstOutput instanceof Map) {
//                    Object content = ((Map<?, ?>) firstOutput).get("content");
//                    if (content instanceof List) {
//                        List<?> contentList = (List<?>) content;
//                        if (!contentList.isEmpty() && contentList.get(0) instanceof Map) {
//                            outputText = ((Map<?, ?>) contentList.get(0)).get("text");
//                        }
//                    }
//                }
//            }
//        }
//        String result = outputText == null ? "" : outputText.toString();
        String result = Optional.ofNullable(response)
                .map(r -> r.get("output"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(firstOutput -> firstOutput.get("content"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(contentList -> !contentList.isEmpty())
                .map(contentList -> contentList.get(0))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(contentMap -> contentMap.get("text")).toString();

        return new LlmResponse(result);
    }

    @Override
    public List<String> getAvailableModels() {
        return Arrays.asList("gpt-3.5-turbo");
    }
}
