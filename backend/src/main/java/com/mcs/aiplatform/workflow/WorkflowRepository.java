package com.mcs.aiplatform.workflow;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {
    List<Workflow> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Workflow> findByIdAndUserId(String id, String userId);
}