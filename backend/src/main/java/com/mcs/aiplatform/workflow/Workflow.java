package com.mcs.aiplatform.workflow;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "workflows")
public class Workflow extends BaseEntity {

    @Indexed
    private String userId;

    private String name;
    private String description;
    /** Optional default agent used for LLM_PROMPT and SUMMARIZE steps. */
    private String agentId;
    private List<WorkflowStep> steps = new ArrayList<>();
    private boolean enabled = true;
}
