package com.mcs.aiplatform.workflow;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class WorkflowStep {
    private String id = UUID.randomUUID().toString();
    private String name;
    private StepType type;
    /** Template for step input. Use {{variableName}} to reference prior context values. */
    private String inputTemplate;
    /** Key under which this step's output is stored in the execution context. */
    private String outputKey;
    /** Step-specific config (e.g., systemPrompt, provider, url). */
    private Map<String, String> config;
}
