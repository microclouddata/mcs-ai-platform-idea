package com.mcs.aiplatform.workflow;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowExecutionRepository extends MongoRepository<WorkflowExecution, String> {
    List<WorkflowExecution> findByWorkflowIdOrderByCreatedAtDesc(String workflowId);
    List<WorkflowExecution> findByUserIdOrderByCreatedAtDesc(String userId);
}
