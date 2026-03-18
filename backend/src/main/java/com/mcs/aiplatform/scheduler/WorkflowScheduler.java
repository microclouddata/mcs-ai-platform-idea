package com.mcs.aiplatform.scheduler;

import com.mcs.aiplatform.workflow.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowScheduler {

    private final ScheduledJobService scheduledJobService;
    private final WorkflowService workflowService;

    /** Checks every 60 seconds for jobs that are due to run. */
    @Scheduled(fixedRate = 60_000)
    public void tick() {
        List<ScheduledJob> due = scheduledJobService.findDueJobs();
        for (ScheduledJob job : due) {
            log.info("Triggering scheduled job '{}' (workflow: {})", job.getName(), job.getWorkflowId());
            try {
                workflowService.execute(job.getWorkflowId(), job.getUserId(), Map.of());
                scheduledJobService.markRan(job);
            } catch (Exception e) {
                log.error("Scheduled job '{}' failed: {}", job.getName(), e.getMessage());
            }
        }
    }
}
