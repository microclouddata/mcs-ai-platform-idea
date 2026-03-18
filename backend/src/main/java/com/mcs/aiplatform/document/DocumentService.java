package com.mcs.aiplatform.document;

import com.mcs.aiplatform.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    @Value("${app.storage.local-path}")
    private String storageRoot;

    private final DocumentFileRepository documentFileRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final TextExtractorService textExtractorService;
    private final EmbeddingService embeddingService;

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

        String text = textExtractorService.extractText(file);
        List<String> chunks = splitText(text, 1000, 150);

        int idx = 0;
        for (String chunkText : chunks) {
            if (chunkText == null || chunkText.isBlank()) {
                continue;
            }

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentId(document.getId());
            chunk.setUserId(userId);
            chunk.setAgentId(agentId);
            chunk.setChunkIndex(idx++);
            chunk.setContent(chunkText);
            chunk.setMetadata(new HashMap<>() {{
                put("fileName", file.getOriginalFilename());
                put("contentType", file.getContentType());
            }});
            chunk.setEmbedding(embeddingService.embed(chunkText));
            documentChunkRepository.save(chunk);
        }

        document.setStatus("PROCESSED");
        return documentFileRepository.save(document);
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }

        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            result.add(text.substring(start, end));

            if (end == length) {
                break;
            }

            start = Math.max(0, end - overlap);
        }

        return result;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unknown";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
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
}
