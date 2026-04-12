package com.mcs.rag.search;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal API consumed by chat-service for knowledge retrieval.
 * No JWT required — called from within the cluster only.
 */
@RestController
@RequestMapping("/internal/rag")
@RequiredArgsConstructor
public class RagInternalController {

    private final SearchService searchService;

    @PostMapping("/search")
    public Map<String, String> search(@RequestBody SearchRequest request) {
        String content = searchService.search(request.agentId(), request.query());
        return Map.of("content", content);
    }

    public record SearchRequest(String agentId, String query) {}
}
