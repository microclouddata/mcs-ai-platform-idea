package com.mcs.aiplatform.admin;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AgentViewRepository extends MongoRepository<AgentView, String> {
    List<AgentView> findAllByOrderByCreatedAtDesc();
}
