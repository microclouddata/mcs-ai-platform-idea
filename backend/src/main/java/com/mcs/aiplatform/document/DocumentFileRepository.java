package com.mcs.aiplatform.document;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentFileRepository extends MongoRepository<DocumentFile, String> {
    List<DocumentFile> findByUserIdAndAgentIdOrderByCreatedAtDesc(String userId, String agentId);
    java.util.Optional<DocumentFile> findByIdAndUserId(String id, String userId);
}
