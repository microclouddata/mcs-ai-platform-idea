package com.mcs.usage.usage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageLogRepository extends MongoRepository<UsageLog, String> {

    List<UsageLog> findByUserId(String userId);

    List<UsageLog> findByAgentId(String agentId);

    Page<UsageLog> findAll(Pageable pageable);
}
