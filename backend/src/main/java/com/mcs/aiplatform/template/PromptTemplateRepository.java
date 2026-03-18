package com.mcs.aiplatform.template;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository extends MongoRepository<PromptTemplate, String> {
    List<PromptTemplate> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<PromptTemplate> findByIdAndUserId(String id, String userId);
}
