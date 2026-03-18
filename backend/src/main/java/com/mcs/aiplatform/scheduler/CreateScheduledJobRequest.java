package com.mcs.aiplatform.scheduler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateScheduledJobRequest(
        @NotBlank String name,
        @NotBlank String workflowId,
        @NotNull ScheduleType scheduleType
) {}
