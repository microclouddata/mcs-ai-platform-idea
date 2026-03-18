package com.mcs.aiplatform.scheduler;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "scheduled_jobs")
public class ScheduledJob extends BaseEntity {

    @Indexed
    private String userId;

    private String name;
    private String workflowId;
    private ScheduleType scheduleType = ScheduleType.DAILY;
    private boolean enabled = true;
    private Instant lastRunAt;
    private Instant nextRunAt;
}
