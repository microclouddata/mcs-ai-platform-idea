package com.mcs.aiplatform.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("OLLAMA")
public class OllamaProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);

    private final String model;
    private final String baseUrl;
    private final RestClient restClient;

    public OllamaProvider(@Value("${app.llm.ollama.base-url:http://localhost:11434}") String baseUrl,
                          @Value("${app.llm.ollama.model:qwen2.5-coder:14b}") String model) {
        this.model = model;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder().baseUrl(baseUrl + "/v1").build();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
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
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(choice -> choice.get("message"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(msg -> msg.get("content"))
                .map(Object::toString)
                .orElse("");

        return new LlmResponse(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAvailableModels() {
        try {
            RestClient tagsClient = RestClient.builder().baseUrl(baseUrl).build();
            Map response = tagsClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .body(Map.class);

            return Optional.ofNullable(response)
                    .map(r -> r.get("models"))
                    .filter(List.class::isInstance)
                    .map(List.class::cast)
                    .map(models -> (List<String>) models.stream()
                            .filter(Map.class::isInstance)
                            .map(m -> ((Map<?, ?>) m).get("name"))
                            .filter(n -> n instanceof String)
                            .map(Object::toString)
                            .collect(Collectors.toList()))
                    .orElse(Collections.singletonList(model));
        } catch (Exception e) {
            log.warn("Failed to fetch Ollama model list: {}", e.getMessage());
            return Collections.singletonList(model);
        }
    }
}
