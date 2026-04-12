package com.mcs.aiplatform.kafka.consumer;

import com.mcs.aiplatform.document.DocumentChunk;
import com.mcs.aiplatform.document.DocumentChunkRepository;
import com.mcs.aiplatform.document.DocumentFileRepository;
import com.mcs.aiplatform.document.TextExtractorService;
import com.mcs.aiplatform.embedding.EmbeddingService;
import com.mcs.aiplatform.kafka.KafkaTopics;
import com.mcs.aiplatform.kafka.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Async document processing pipeline.
 *
 * <p>Consumes {@code document.uploaded} events and runs the heavy pipeline
 * (text extraction → chunking → embedding generation) outside the HTTP request thread,
 * so the upload endpoint returns immediately with status {@code PROCESSING}.
 *
 * <pre>
 * document.uploaded
 *   → extractText (PDF / DOCX / plain text)
 *   → splitText into overlapping chunks
 *   → embed each chunk via OpenAI / mock
 *   → persist DocumentChunk records
 *   → update DocumentFile status to PROCESSED (or FAILED)
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingConsumer {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 150;

    private final DocumentFileRepository documentFileRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final TextExtractorService textExtractorService;
    private final EmbeddingService embeddingService;

    @KafkaListener(
            topics = KafkaTopics.DOCUMENT_UPLOADED,
            groupId = "document-processor",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processDocument(DocumentUploadedEvent event) {
        log.info("[Kafka] Received document.uploaded: documentId={}, file={}",
                event.documentId(), event.originalFileName());

        try {
            // 1. Extract text from the already-persisted file on disk
            String text = textExtractorService.extractTextFromPath(
                    Paths.get(event.storagePath()), event.originalFileName());

            // 2. Split into overlapping chunks
            List<String> chunks = splitText(text, CHUNK_SIZE, CHUNK_OVERLAP);
            log.info("[Kafka] Document {} split into {} chunks", event.documentId(), chunks.size());

            // 3. Embed and persist each chunk
            int idx = 0;
            for (String chunkText : chunks) {
                if (chunkText == null || chunkText.isBlank()) continue;

                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocumentId(event.documentId());
                chunk.setUserId(event.userId());
                chunk.setAgentId(event.agentId());
                chunk.setChunkIndex(idx++);
                chunk.setContent(chunkText);
                chunk.setMetadata(Map.of(
                        "fileName", event.originalFileName(),
                        "contentType", event.contentType() != null ? event.contentType() : ""
                ));
                chunk.setEmbedding(embeddingService.embed(chunkText));
                documentChunkRepository.save(chunk);
            }

            // 4. Mark document as PROCESSED
            documentFileRepository.findById(event.documentId()).ifPresent(doc -> {
                doc.setStatus("PROCESSED");
                documentFileRepository.save(doc);
            });

            log.info("[Kafka] Document {} processed: {} chunks embedded", event.documentId(), idx);

        } catch (Exception e) {
            log.error("[Kafka] Failed to process document {}: {}", event.documentId(), e.getMessage(), e);

            // Mark document as FAILED so the UI can surface the error
            documentFileRepository.findById(event.documentId()).ifPresent(doc -> {
                doc.setStatus("FAILED");
                documentFileRepository.save(doc);
            });
        }
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;
        int start = 0;
        int length = text.length();
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            result.add(text.substring(start, end));
            if (end == length) break;
            start = Math.max(0, end - overlap);
        }
        return result;
    }
}
