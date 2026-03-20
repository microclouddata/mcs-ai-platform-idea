package com.mcs.aiplatform.nugget;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NuggetRepository extends MongoRepository<Nugget, String> {

    List<Nugget> findByAgentId(String agentId);

    List<Nugget> findByAgentIdAndStatus(String agentId, NuggetStatus status);

    Optional<Nugget> findByIdAndAgentId(String id, String agentId);

    void deleteByAgentId(String agentId);
}
