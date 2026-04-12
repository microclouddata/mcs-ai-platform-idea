package com.mcs.chat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP client for rag-service internal search API.
 */
@Component
public class RagServiceClient {

    private final RestClient restClient;

    public RagServiceClient(@Value("${services.rag-service.url:http://localhost:8085}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Performs knowledge search and returns relevant context as a string.
     */
    public String search(String agentId, String query) {
        try {
            Map<?, ?> result = restClient.post()
                    .uri("/internal/rag/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("agentId", agentId, "query", query))
                    .retrieve()
                    .body(Map.class);
            if (result != null && result.get("content") != null) {
                return result.get("content").toString();
            }
        } catch (Exception e) {
            // Knowledge search failure is non-fatal — continue without context
        }
        return "";
    }
}
