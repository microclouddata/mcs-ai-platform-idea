package com.mcs.aiplatform.integration;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface IntegrationRepository extends MongoRepository<Integration, String> {
    List<Integration> findByUserId(String userId);
    List<Integration> findByUserIdAndType(String userId, IntegrationType type);
    List<Integration> findByUserIdAndEnabledTrue(String userId);
}
