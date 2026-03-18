package com.mcs.aiplatform.tool;

import com.mcs.aiplatform.document.DocumentChunk;
import com.mcs.aiplatform.document.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeSearchTool {

    private final DocumentChunkRepository documentChunkRepository;

    public ToolResult search(String agentId, String query) {
        List<String> keywords = Arrays.stream(query.toLowerCase(Locale.ROOT).split("\\W+"))
                .filter(s -> s.length() > 2)
                .distinct()
                .toList();

        String content = documentChunkRepository.findByAgentId(agentId).stream()
                .map(chunk -> new RankedChunk(chunk, score(chunk.getContent(), keywords)))
                .filter(ranked -> ranked.score() > 0)
                .sorted(Comparator.comparingInt(RankedChunk::score).reversed())
                .limit(5)
                .map(ranked -> ranked.chunk().getContent())
                .collect(Collectors.joining("\n\n---\n\n"));

        if (content.isBlank()) {
            content = "No matching knowledge found in uploaded documents.";
        }
        return new ToolResult("KNOWLEDGE_SEARCH", content);
    }

    private int score(String text, List<String> keywords) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) score++;
        }
        return score;
    }

    private record RankedChunk(DocumentChunk chunk, int score) {}
}
