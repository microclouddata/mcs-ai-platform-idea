package com.mcs.agent.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public Agent create(String userId, CreateAgentRequest request) {
        Agent agent = new Agent();
        agent.setUserId(userId);
        agent.setName(request.name());
        agent.setDescription(defaultValue(request.description(), "AI Agent"));
        agent.setSystemPrompt(defaultValue(request.systemPrompt(), "You are a helpful assistant. Use provided knowledge context when relevant."));
        if (request.provider() != null && !request.provider().isBlank()) agent.setProvider(request.provider());
        if (request.model() != null && !request.model().isBlank()) agent.setModel(request.model());
        if (request.temperature() != null) agent.setTemperature(request.temperature());
        agent.setTools(request.tools() == null || request.tools().isEmpty() ? List.of("KNOWLEDGE_SEARCH") : request.tools());
        if (request.memoryEnabled() != null) agent.setMemoryEnabled(request.memoryEnabled());
        if (request.toolsEnabled() != null) agent.setToolsEnabled(request.toolsEnabled());
        return agentRepository.save(agent);
    }

    public List<Agent> list(String userId) {
        return agentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Agent get(String userId, String agentId) {
        return agentRepository.findByIdAndUserId(agentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
    }

    @Cacheable(value = "agents", key = "#agentId")
    public Agent getForUse(String agentId, String userId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        if (agent.getUserId().equals(userId)) return agent;
        if (!agent.isEnabled()) throw new IllegalArgumentException("Agent not found");
        throw new IllegalArgumentException("Agent not found");
    }

    @CacheEvict(value = "agents", key = "#agentId")
    public Agent update(String userId, String agentId, UpdateAgentRequest request) {
        Agent agent = get(userId, agentId);
        if (request.name() != null) agent.setName(request.name());
        if (request.description() != null) agent.setDescription(request.description());
        if (request.systemPrompt() != null) agent.setSystemPrompt(request.systemPrompt());
        if (request.provider() != null) agent.setProvider(request.provider());
        if (request.model() != null) agent.setModel(request.model());
        if (request.temperature() != null) agent.setTemperature(request.temperature());
        if (request.tools() != null) agent.setTools(request.tools());
        if (request.memoryEnabled() != null) agent.setMemoryEnabled(request.memoryEnabled());
        if (request.toolsEnabled() != null) agent.setToolsEnabled(request.toolsEnabled());
        return agentRepository.save(agent);
    }

    @CacheEvict(value = "agents", key = "#agentId")
    public Agent toggleEnabled(String userId, String agentId) {
        Agent agent = get(userId, agentId);
        agent.setEnabled(!agent.isEnabled());
        return agentRepository.save(agent);
    }

    @CacheEvict(value = "agents", key = "#agentId")
    public void delete(String userId, String agentId) {
        Agent agent = get(userId, agentId);
        agentRepository.delete(agent);
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
