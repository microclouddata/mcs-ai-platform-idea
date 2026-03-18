package com.mcs.aiplatform.workflow;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "workflow_executions")
public class WorkflowExecution extends BaseEntity {

    @Indexed
    private String workflowId;

    @Indexed
    private String userId;

    private ExecutionStatus status = ExecutionStatus.PENDING;
    private Map<String, String> input = new HashMap<>();
    private Map<String, String> context = new HashMap<>();
    private List<StepResult> stepResults = new ArrayList<>();
    private String finalOutput;
    private String error;
    private Instant startedAt;
    private Instant finishedAt;
}
