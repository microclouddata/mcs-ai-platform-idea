package com.mcs.aiplatform.tool;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AgentToolBindingRepository extends MongoRepository<AgentToolBinding, String> {
    List<AgentToolBinding> findByAgentId(String agentId);
    List<AgentToolBinding> findByAgentIdAndEnabled(String agentId, boolean enabled);
    void deleteByAgentId(String agentId);
}