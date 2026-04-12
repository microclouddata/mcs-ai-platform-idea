package com.mcs.rag.kafka.event;

public record DocumentUploadedEvent(
        String documentId,
        String userId,
        String agentId,
        String storagePath,
        String fileName,
        String contentType
) {}
