package com.mcs.rag.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Generates text embeddings via OpenAI's embedding API.
 * Falls back to a zero-vector when mock mode is enabled or no API key is set.
 */
@Slf4j
@Service
public class EmbeddingService {

    private static final int EMBEDDING_DIM = 1536;

    private final RestClient restClient;
    private final String model;
    private final boolean mockMode;

    public EmbeddingService(
            @Value("${llm.openai.api-key:}") String apiKey,
            @Value("${llm.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${llm.embedding.model:text-embedding-3-small}") String model,
            @Value("${llm.mock-mode:false}") boolean mockMode) {
        this.model = model;
        this.mockMode = mockMode || apiKey == null || apiKey.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public List<Double> embed(String text) {
        if (mockMode) {
            return zeroVector();
        }
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("input", text, "model", model))
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                List<?> data = (List<?>) response.get("data");
                if (data != null && !data.isEmpty()) {
                    Map<?, ?> first = (Map<?, ?>) data.get(0);
                    List<?> raw = (List<?>) first.get("embedding");
                    return raw.stream()
                            .map(v -> ((Number) v).doubleValue())
                            .toList();
                }
            }
        } catch (Exception e) {
            log.warn("Embedding call failed, using zero vector: {}", e.getMessage());
        }
        return zeroVector();
    }

    public double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size()) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private List<Double> zeroVector() {
        return java.util.Collections.nCopies(EMBEDDING_DIM, 0.0);
    }
}
