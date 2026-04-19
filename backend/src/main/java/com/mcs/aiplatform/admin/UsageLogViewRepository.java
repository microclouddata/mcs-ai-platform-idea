package com.mcs.aiplatform.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsageLogViewRepository extends MongoRepository<UsageLogView, String> {
    Page<UsageLogView> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
