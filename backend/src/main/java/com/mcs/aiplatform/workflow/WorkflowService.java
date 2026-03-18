package com.mcs.aiplatform.workflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowExecutionService executionService;

    public Workflow create(String userId, CreateWorkflowRequest req) {
        Workflow w = new Workflow();
        w.setUserId(userId);
        w.setName(req.name());
        w.setDescription(req.description() != null ? req.description() : "");
        w.setAgentId(req.agentId());
        if (req.steps() != null) w.setSteps(req.steps());
        return workflowRepository.save(w);
    }

    public List<Workflow> listByUser(String userId) {
        return workflowRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Workflow getOwned(String id, String userId) {
        return workflowRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));
    }

    public Workflow update(String id, String userId, CreateWorkflowRequest req) {
        Workflow w = getOwned(id, userId);
        if (req.name() != null) w.setName(req.name());
        if (req.description() != null) w.setDescription(req.description());
        if (req.agentId() != null) w.setAgentId(req.agentId());
        if (req.steps() != null) w.setSteps(req.steps());
        return workflowRepository.save(w);
    }

    public void delete(String id, String userId) {
        workflowRepository.delete(getOwned(id, userId));
    }

    public WorkflowExecution execute(String workflowId, String userId, Map<String, String> input) {
        Workflow workflow = getOwned(workflowId, userId);
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setUserId(userId);
        execution.setInput(input != null ? input : Map.of());
        execution.setStatus(ExecutionStatus.PENDING);
        execution = executionRepository.save(execution);
        executionService.runAsync(execution.getId(), workflow);
        return execution;
    }

    public List<WorkflowExecution> listExecutions(String workflowId, String userId) {
        getOwned(workflowId, userId); // ownership check
        return executionRepository.findByWorkflowIdOrderByCreatedAtDesc(workflowId);
    }

    public WorkflowExecution getExecution(String executionId) {
        return executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found"));
    }
}
