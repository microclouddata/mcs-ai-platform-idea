package com.mcs.rag.kafka.consumer;

import com.mcs.rag.document.DocumentChunk;
import com.mcs.rag.document.DocumentChunkRepository;
import com.mcs.rag.document.DocumentFile;
import com.mcs.rag.document.DocumentFileRepository;
import com.mcs.rag.document.TextExtractorService;
import com.mcs.rag.embedding.EmbeddingService;
import com.mcs.rag.kafka.KafkaTopics;
import com.mcs.rag.kafka.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Consumes document.uploaded events and runs the RAG processing pipeline:
 * text extraction → chunking → embedding → storage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentConsumer {

    private static final int CHUNK_SIZE = 512;
    private static final int CHUNK_OVERLAP = 64;

    private final DocumentFileRepository documentFileRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final TextExtractorService textExtractorService;
    private final EmbeddingService embeddingService;

    @KafkaListener(topics = KafkaTopics.DOCUMENT_UPLOADED, groupId = "rag-service")
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Processing document: {}", event.documentId());
        try {
            String text = textExtractorService.extractTextFromPath(
                    Paths.get(event.storagePath()), event.contentType());

            List<String> chunks = chunk(text);
            List<DocumentChunk> chunkEntities = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocumentId(event.documentId());
                chunk.setUserId(event.userId());
                chunk.setAgentId(event.agentId());
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunk.setEmbedding(embeddingService.embed(chunks.get(i)));
                chunkEntities.add(chunk);
            }
            documentChunkRepository.saveAll(chunkEntities);

            updateStatus(event.documentId(), DocumentFile.DocumentStatus.PROCESSED);
            log.info("Document {} processed: {} chunks", event.documentId(), chunks.size());
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", event.documentId(), e.getMessage(), e);
            updateStatus(event.documentId(), DocumentFile.DocumentStatus.FAILED);
        }
    }

    private List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;
        String[] words = text.split("\\s+");
        int start = 0;
        while (start < words.length) {
            int end = Math.min(start + CHUNK_SIZE, words.length);
            chunks.add(String.join(" ", java.util.Arrays.copyOfRange(words, start, end)));
            start += CHUNK_SIZE - CHUNK_OVERLAP;
        }
        return chunks;
    }

    private void updateStatus(String documentId, DocumentFile.DocumentStatus status) {
        documentFileRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(status);
            documentFileRepository.save(doc);
        });
    }
}
