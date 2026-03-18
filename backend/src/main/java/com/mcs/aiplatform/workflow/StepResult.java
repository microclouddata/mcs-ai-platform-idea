package com.mcs.aiplatform.workflow;

import lombok.Data;

@Data
public class StepResult {
    private String stepId;
    private String stepName;
    private StepResultStatus status;
    private String output;
    private String error;
    private long durationMs;
}
