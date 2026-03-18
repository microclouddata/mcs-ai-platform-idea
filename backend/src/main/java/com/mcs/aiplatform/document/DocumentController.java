package com.mcs.aiplatform.document;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ApiResponse<DocumentFile> upload(@RequestParam String agentId,
                                            @RequestParam("file") MultipartFile file) throws Exception {
        String userId = CurrentUser.userId();
        return ApiResponse.ok(documentService.upload(userId, agentId, file));
    }

    @GetMapping
    public ApiResponse<List<DocumentFile>> list(@RequestParam String agentId) {
        String userId = CurrentUser.userId();
        return ApiResponse.ok(documentService.list(userId, agentId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        String userId = CurrentUser.userId();
        DocumentFile doc = documentService.getOwned(userId, id);
        Resource resource = new PathResource(Paths.get(doc.getStoragePath()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(
                        doc.getContentType() != null ? doc.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) throws Exception {
        documentService.delete(CurrentUser.userId(), id);
        return ApiResponse.ok(null);
    }
}
