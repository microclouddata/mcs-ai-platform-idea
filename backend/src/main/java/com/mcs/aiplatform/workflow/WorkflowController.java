package com.mcs.aiplatform.workflow;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public ApiResponse<Workflow> create(@Valid @RequestBody CreateWorkflowRequest req) {
        return ApiResponse.ok(workflowService.create(CurrentUser.userId(), req));
    }

    @GetMapping
    public ApiResponse<List<Workflow>> list() {
        return ApiResponse.ok(workflowService.listByUser(CurrentUser.userId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Workflow> get(@PathVariable String id) {
        return ApiResponse.ok(workflowService.getOwned(id, CurrentUser.userId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<Workflow> update(@PathVariable String id, @RequestBody CreateWorkflowRequest req) {
        return ApiResponse.ok(workflowService.update(id, CurrentUser.userId(), req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        workflowService.delete(id, CurrentUser.userId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<WorkflowExecution> execute(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> input) {
        return ApiResponse.ok(workflowService.execute(id, CurrentUser.userId(), input));
    }

    @GetMapping("/{id}/executions")
    public ApiResponse<List<WorkflowExecution>> executions(@PathVariable String id) {
        return ApiResponse.ok(workflowService.listExecutions(id, CurrentUser.userId()));
    }

    @GetMapping("/executions/{executionId}")
    public ApiResponse<WorkflowExecution> getExecution(@PathVariable String executionId) {
        return ApiResponse.ok(workflowService.getExecution(executionId));
    }
}
