package com.mcs.aiplatform.chat;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.ok(chatService.chat(CurrentUser.userId(), request));
    }

    @GetMapping("/history/{sessionId}")
    public ApiResponse<?> history(@PathVariable String sessionId) {
        return ApiResponse.ok(chatService.history(sessionId));
    }
}
