package com.mcs.chat.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * HTTP client for agent-service internal API.
 * Fetches agent config and skill content on behalf of chat requests.
 */
@Component
public class AgentServiceClient {

    private final RestClient restClient;

    public AgentServiceClient(@Value("${services.agent-service.url:http://localhost:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public AgentDto getAgent(String agentId) {
        return restClient.get()
                .uri("/internal/agents/{id}", agentId)
                .retrieve()
                .body(AgentDto.class);
    }

    public List<SkillDto> getActiveSkills(String agentId) {
        return restClient.get()
                .uri("/internal/agents/{id}/skills/active", agentId)
                .retrieve()
                .bodyList(SkillDto.class);
    }

    public SkillDto getSkillById(String agentId, String skillId) {
        return restClient.get()
                .uri("/internal/agents/{agentId}/skills/{skillId}", agentId, skillId)
                .retrieve()
                .body(SkillDto.class);
    }

    public SkillDto getSkillByName(String agentId, String name) {
        return restClient.get()
                .uri("/internal/agents/{agentId}/skills/by-name/{name}", agentId, name)
                .retrieve()
                .body(SkillDto.class);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentDto {
        private String id;
        private String userId;
        private String name;
        private String systemPrompt;
        private String provider;
        private String model;
        private double temperature = 0.2;
        private boolean memoryEnabled;
        private boolean toolsEnabled;
        private boolean enabled;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillDto {
        private String id;
        private String name;
        private String instructions;
        private String status;
    }
}
