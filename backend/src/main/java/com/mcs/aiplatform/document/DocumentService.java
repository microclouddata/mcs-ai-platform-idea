package com.mcs.aiplatform.document;

import com.mcs.aiplatform.kafka.event.DocumentUploadedEvent;
import com.mcs.aiplatform.kafka.producer.DocumentEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    @Value("${app.storage.local-path}")
    private String storageRoot;

    private final DocumentFileRepository documentFileRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentEventProducer documentEventProducer;

    /**
     * Saves the uploaded file to disk, creates a {@code DocumentFile} record with
     * status {@code PROCESSING}, then publishes a {@code document.uploaded} Kafka event
     * and returns immediately.
     *
     * <p>The heavy pipeline (text extraction → chunking → embedding) runs asynchronously
     * in {@code DocumentProcessingConsumer}, which updates the status to {@code PROCESSED}
     * (or {@code FAILED}) when done.
     */
    public DocumentFile upload(String userId, String agentId, MultipartFile file) throws IOException {
        Path dir = Paths.get(storageRoot);
        Files.createDirectories(dir);

        String safeName = System.currentTimeMillis() + "_" + sanitizeFileName(file.getOriginalFilename());
        Path target = dir.resolve(safeName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        DocumentFile document = new DocumentFile();
        document.setUserId(userId);
        document.setAgentId(agentId);
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setStoragePath(target.toString());
        document.setStatus("PROCESSING");
        document.setSize(file.getSize());
        document = documentFileRepository.save(document);

        // Publish event — async processing runs in DocumentProcessingConsumer
        documentEventProducer.publishDocumentUploaded(new DocumentUploadedEvent(
                document.getId(),
                userId,
                agentId,
                target.toString(),
                file.getOriginalFilename(),
                file.getContentType()
        ));

        return document;
    }

    public List<DocumentFile> list(String userId, String agentId) {
        return documentFileRepository.findByUserIdAndAgentIdOrderByCreatedAtDesc(userId, agentId);
    }

    public DocumentFile getOwned(String userId, String documentId) {
        return documentFileRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    public void delete(String userId, String documentId) throws IOException {
        DocumentFile doc = getOwned(userId, documentId);
        if (doc.getStoragePath() != null) {
            Files.deleteIfExists(Paths.get(doc.getStoragePath()));
        }
        documentChunkRepository.deleteByDocumentId(doc.getId());
        documentFileRepository.delete(doc);
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "unknown";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
