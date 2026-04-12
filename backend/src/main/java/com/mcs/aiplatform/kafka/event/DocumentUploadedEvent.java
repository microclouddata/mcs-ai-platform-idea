package com.mcs.aiplatform.kafka.event;

/**
 * Emitted by {@code DocumentService} immediately after a file is persisted to disk
 * and a {@code DocumentFile} record with status {@code PROCESSING} is saved.
 *
 * <p>The {@code DocumentProcessingConsumer} reads this event and runs the slow
 * text-extraction → chunking → embedding pipeline asynchronously, so the HTTP
 * upload request can return without waiting for embedding generation.
 */
public record DocumentUploadedEvent(
        String documentId,
        String userId,
        String agentId,
        String storagePath,
        String originalFileName,
        String contentType
) {}
