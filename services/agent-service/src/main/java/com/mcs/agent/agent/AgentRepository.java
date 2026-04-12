package com.mcs.agent.agent;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends MongoRepository<Agent, String> {
    List<Agent> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Agent> findByIdAndUserId(String id, String userId);
}
