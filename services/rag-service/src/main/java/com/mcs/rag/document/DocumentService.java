package com.mcs.rag.document;

import com.mcs.rag.kafka.event.DocumentUploadedEvent;
import com.mcs.rag.kafka.producer.DocumentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentFileRepository documentFileRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentEventProducer documentEventProducer;

    @Value("${app.storage.local-path:/app/uploads}")
    private String storagePath;

    public DocumentFile upload(String userId, String agentId, MultipartFile file) throws IOException {
        // Ensure storage directory exists
        Path storageDir = Paths.get(storagePath);
        Files.createDirectories(storageDir);

        // Generate unique filename to avoid collisions
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = storageDir.resolve(uniqueFileName);

        // Save file to disk
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Persist DocumentFile record with PROCESSING status
        DocumentFile documentFile = new DocumentFile();
        documentFile.setUserId(userId);
        documentFile.setAgentId(agentId);
        documentFile.setFileName(originalFilename);
        documentFile.setContentType(file.getContentType());
        documentFile.setStoragePath(targetPath.toAbsolutePath().toString());
        documentFile.setStatus(DocumentFile.DocumentStatus.PROCESSING);
        documentFile.setSize(file.getSize());

        documentFile = documentFileRepository.save(documentFile);
        log.info("Saved document file {} for user {} agent {}", documentFile.getId(), userId, agentId);

        // Publish Kafka event to trigger async processing pipeline
        DocumentUploadedEvent event = new DocumentUploadedEvent(
                documentFile.getId(),
                userId,
                agentId,
                targetPath.toAbsolutePath().toString(),
                originalFilename,
                file.getContentType()
        );
        documentEventProducer.publishDocumentUploaded(event);

        return documentFile;
    }

    public List<DocumentFile> list(String userId, String agentId) {
        return documentFileRepository.findByUserIdAndAgentIdOrderByCreatedAtDesc(userId, agentId);
    }

    public DocumentFile getOwned(String documentId, String userId) {
        return documentFileRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or access denied"));
    }

    public Resource loadAsResource(String documentId, String userId) throws MalformedURLException {
        DocumentFile documentFile = getOwned(documentId, userId);
        Path filePath = Paths.get(documentFile.getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("File not found or not readable: " + documentFile.getFileName());
        }
        return resource;
    }

    public void delete(String documentId, String userId) {
        DocumentFile documentFile = getOwned(documentId, userId);

        // Delete associated chunks first
        documentChunkRepository.deleteByDocumentId(documentId);

        // Delete file from disk
        try {
            Path filePath = Paths.get(documentFile.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file from disk for document {}: {}", documentId, e.getMessage());
        }

        // Delete document record
        documentFileRepository.deleteById(documentId);
        log.info("Deleted document {} for user {}", documentId, userId);
    }
}
