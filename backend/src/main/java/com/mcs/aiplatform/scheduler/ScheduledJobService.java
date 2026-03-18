package com.mcs.aiplatform.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledJobService {

    private final ScheduledJobRepository repository;

    public ScheduledJob create(String userId, CreateScheduledJobRequest req) {
        ScheduledJob job = new ScheduledJob();
        job.setUserId(userId);
        job.setName(req.name());
        job.setWorkflowId(req.workflowId());
        job.setScheduleType(req.scheduleType());
        job.setNextRunAt(computeNext(req.scheduleType(), Instant.now()));
        return repository.save(job);
    }

    public List<ScheduledJob> listByUser(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public ScheduledJob getOwned(String id, String userId) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled job not found"));
    }

    public ScheduledJob toggleEnabled(String id, String userId) {
        ScheduledJob job = getOwned(id, userId);
        job.setEnabled(!job.isEnabled());
        if (job.isEnabled()) {
            job.setNextRunAt(computeNext(job.getScheduleType(), Instant.now()));
        }
        return repository.save(job);
    }

    public void delete(String id, String userId) {
        repository.delete(getOwned(id, userId));
    }

    public List<ScheduledJob> findDueJobs() {
        return repository.findByEnabledTrue().stream()
                .filter(j -> j.getNextRunAt() != null && j.getNextRunAt().isBefore(Instant.now()))
                .toList();
    }

    public void markRan(ScheduledJob job) {
        job.setLastRunAt(Instant.now());
        job.setNextRunAt(computeNext(job.getScheduleType(), Instant.now()));
        repository.save(job);
    }

    private Instant computeNext(ScheduleType type, Instant from) {
        return switch (type) {
            case HOURLY -> from.plus(1, ChronoUnit.HOURS);
            case DAILY  -> from.plus(1, ChronoUnit.DAYS);
            case WEEKLY -> from.plus(7, ChronoUnit.DAYS);
        };
    }
}
