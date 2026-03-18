package com.mcs.aiplatform.scheduler;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduledJobRepository extends MongoRepository<ScheduledJob, String> {
    List<ScheduledJob> findByUserIdOrderByCreatedAtDesc(String userId);
    List<ScheduledJob> findByEnabledTrue();
    Optional<ScheduledJob> findByIdAndUserId(String id, String userId);
}
