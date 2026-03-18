package com.mcs.aiplatform.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    @Value("${llm.openai.api-key:}")
    private String apiKey;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${llm.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    @Value("${llm.mock-mode:false}")
    private boolean mockMode;

    @SuppressWarnings("unchecked")
    public List<Double> embed(String text) {
        if (mockMode || apiKey == null || apiKey.isBlank()) {
            return Collections.nCopies(1536, 0.0);
        }
        try {
            var client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            var response = client.post()
                    .uri("/embeddings")
                    .body(Map.of("model", embeddingModel, "input", text))
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                var data = (List<Map<String, Object>>) response.get("data");
                if (data != null && !data.isEmpty()) {
                    return (List<Double>) data.get(0).get("embedding");
                }
            }
        } catch (Exception e) {
            log.warn("Embedding API call failed: {}", e.getMessage());
        }
        return Collections.nCopies(1536, 0.0);
    }

    public double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size()) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        return (normA == 0 || normB == 0) ? 0.0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}