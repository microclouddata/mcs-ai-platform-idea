package com.mcs.rag.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "document_chunks")
@CompoundIndex(def = "{'agentId': 1, 'documentId': 1, 'chunkIndex': 1}")
public class DocumentChunk {

    @Id
    private String id;
    private String documentId;
    private String userId;
    private String agentId;
    private Integer chunkIndex;
    private String content;
    private List<Double> embedding;
    private Map<String, Object> metadata;
    private Instant createdAt = Instant.now();
}
