package com.mcs.aiplatform.tool.executor;

import com.mcs.aiplatform.document.DocumentChunk;
import com.mcs.aiplatform.document.DocumentChunkRepository;
import com.mcs.aiplatform.embedding.EmbeddingService;
import com.mcs.aiplatform.tool.AgentToolBinding;
import com.mcs.aiplatform.tool.ToolType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeSearchExecutor implements ToolExecutor {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    @Override
    public ToolType supportedType() {
        return ToolType.KNOWLEDGE_SEARCH;
    }

    @Override
    public String execute(String query, AgentToolBinding binding, String agentId) {
        List<DocumentChunk> chunks = chunkRepository.findByAgentId(agentId);
        if (chunks.isEmpty()) return "";

        boolean hasEmbeddings = chunks.stream()
                .anyMatch(c -> c.getEmbedding() != null && !c.getEmbedding().isEmpty());

        List<DocumentChunk> ranked;
        if (hasEmbeddings) {
            List<Double> queryEmbedding = embeddingService.embed(query);
            ranked = chunks.stream()
                    .filter(c -> c.getEmbedding() != null && !c.getEmbedding().isEmpty())
                    .sorted(Comparator.comparingDouble(c ->
                            -embeddingService.cosineSimilarity(queryEmbedding, c.getEmbedding())))
                    .limit(5)
                    .collect(Collectors.toList());
        } else {
            ranked = keywordSearch(query, chunks);
        }

        return ranked.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n---\n"));
    }

    private List<DocumentChunk> keywordSearch(String query, List<DocumentChunk> chunks) {
        String[] keywords = query.toLowerCase().split("\\W+");
        return chunks.stream()
                .filter(c -> c.getContent() != null)
                .sorted(Comparator.comparingLong((DocumentChunk c) ->
                        Arrays.stream(keywords)
                                .filter(k -> k.length() > 2 && c.getContent().toLowerCase().contains(k))
                                .count()).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
}