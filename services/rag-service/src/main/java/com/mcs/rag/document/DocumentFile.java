package com.mcs.rag.document;

import com.mcs.rag.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "document_files")
@CompoundIndex(def = "{'userId': 1, 'agentId': 1, 'createdAt': -1}")
public class DocumentFile extends BaseEntity {

    private String userId;
    private String agentId;
    private String fileName;
    private String contentType;
    private String storagePath;
    private DocumentStatus status;
    private Long size;

    public enum DocumentStatus {
        PROCESSING,
        PROCESSED,
        FAILED
    }
}
