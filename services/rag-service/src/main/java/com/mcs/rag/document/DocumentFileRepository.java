package com.mcs.rag.document;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentFileRepository extends MongoRepository<DocumentFile, String> {

    List<DocumentFile> findByUserIdAndAgentIdOrderByCreatedAtDesc(String userId, String agentId);

    Optional<DocumentFile> findByIdAndUserId(String id, String userId);
}
