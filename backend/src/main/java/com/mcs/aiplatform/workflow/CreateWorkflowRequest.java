package com.mcs.aiplatform.workflow;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateWorkflowRequest(
        @NotBlank String name,
        String description,
        String agentId,
        List<WorkflowStep> steps
) {}
