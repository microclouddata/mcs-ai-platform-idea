package com.mcs.rag.document;

import com.mcs.rag.common.ApiResponse;
import com.mcs.rag.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentFile>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("agentId") String agentId) throws IOException {
        String userId = CurrentUser.userId();
        DocumentFile documentFile = documentService.upload(userId, agentId, file);
        return ResponseEntity.ok(ApiResponse.ok(documentFile));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentFile>>> list(
            @RequestParam("agentId") String agentId) {
        String userId = CurrentUser.userId();
        List<DocumentFile> documents = documentService.list(userId, agentId);
        return ResponseEntity.ok(ApiResponse.ok(documents));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        String userId = CurrentUser.userId();
        DocumentFile documentFile = documentService.getOwned(id, userId);
        Resource resource = documentService.loadAsResource(id, userId);

        String contentType = documentFile.getContentType() != null
                ? documentFile.getContentType()
                : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentFile.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        String userId = CurrentUser.userId();
        documentService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
