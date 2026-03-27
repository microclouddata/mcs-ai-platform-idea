package com.mcs.aiplatform.chat;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import com.mcs.aiplatform.document.TextExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final TextExtractorService textExtractorService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.ok(chatService.chat(CurrentUser.userId(), request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ChatResponse> chatWithFiles(
            @RequestParam String agentId,
            @RequestParam(required = false) String sessionId,
            @RequestParam String message,
            @RequestParam(required = false) String skillId,
            @RequestParam(required = false) List<MultipartFile> files) {

        String attachmentContext = extractAttachmentContext(files);
        String fullMessage = attachmentContext.isBlank() ? message : message + "\n\n" + attachmentContext;

        ChatRequest request = new ChatRequest(agentId, sessionId, fullMessage, skillId);
        return ApiResponse.ok(chatService.chat(CurrentUser.userId(), request));
    }

    private String extractAttachmentContext(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return "";
        List<String> parts = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String content = textExtractorService.extractText(file);
                parts.add("--- Attached file: " + file.getOriginalFilename() + " ---\n" + content);
            } catch (Exception e) {
                log.warn("Could not extract text from {}: {}", file.getOriginalFilename(), e.getMessage());
                parts.add("[Could not read file: " + file.getOriginalFilename() + "]");
            }
        }
        return String.join("\n\n", parts);
    }

    @GetMapping("/history/{sessionId}")
    public ApiResponse<?> history(@PathVariable String sessionId) {
        return ApiResponse.ok(chatService.history(sessionId));
    }
}
