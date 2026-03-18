package com.mcs.aiplatform.usage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UsageLogRepository extends MongoRepository<UsageLog, String> {
    List<UsageLog> findByUserId(String userId);
    List<UsageLog> findByAgentId(String agentId);
    Page<UsageLog> findAll(Pageable pageable);
}