package com.mcs.aiplatform.document;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentChunkRepository extends MongoRepository<DocumentChunk, String> {
    List<DocumentChunk> findByAgentId(String agentId);
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(String documentId);
    void deleteByDocumentId(String documentId);
}
