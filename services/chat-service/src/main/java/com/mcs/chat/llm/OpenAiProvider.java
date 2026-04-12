package com.mcs.chat.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("OPENAI")
public class OpenAiProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final String apiKey;
    private final String model;
    private final boolean mockMode;
    private final RestClient restClient;

    public OpenAiProvider(@Value("${app.llm.openai.api-key:}") String apiKey,
                          @Value("${app.llm.openai.base-url:https://api.openai.com}") String baseUrl,
                          @Value("${app.llm.openai.model:gpt-3.5-turbo}") String model,
                          @Value("${app.llm.mock-enabled:false}") boolean mockMode) {
        this.apiKey = apiKey;
        this.model = model;
        this.mockMode = mockMode;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl + "/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResponse chat(LlmRequest request) {
        if (mockMode || apiKey == null || apiKey.isBlank()) {
            return new LlmResponse("[Mock] Response to: " + request.userPrompt());
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", request.systemPrompt()),
                        Map.of("role", "user", "content", request.userPrompt())
                )
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        String result = Optional.ofNullable(response)
                .map(r -> r.get("choices"))
                .filter(List.class::isInstance).map(List.class::cast)
                .filter(list -> !list.isEmpty()).map(list -> list.get(0))
                .filter(Map.class::isInstance).map(Map.class::cast)
                .map(choice -> choice.get("message"))
                .filter(Map.class::isInstance).map(Map.class::cast)
                .map(msg -> msg.get("content"))
                .map(Object::toString).orElse("");

        return new LlmResponse(result);
    }

    @Override
    public List<String> getAvailableModels() {
        return List.of("gpt-3.5-turbo", "gpt-4", "gpt-4o", "gpt-4o-mini");
    }
}
