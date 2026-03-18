package com.mcs.aiplatform.apikey;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    List<ApiKey> findByUserId(String userId);
    Optional<ApiKey> findByKeyHashAndEnabledTrue(String keyHash);
    void deleteByIdAndUserId(String id, String userId);
}
