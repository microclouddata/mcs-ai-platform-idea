package com.mcs.aiplatform.knowledgebase;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseRepository extends MongoRepository<KnowledgeBase, String> {
    List<KnowledgeBase> findByUserId(String userId);
    Optional<KnowledgeBase> findByIdAndUserId(String id, String userId);
}