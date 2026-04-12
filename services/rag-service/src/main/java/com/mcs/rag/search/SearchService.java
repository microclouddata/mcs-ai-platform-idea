package com.mcs.rag.search;

import com.mcs.rag.document.DocumentChunk;
import com.mcs.rag.document.DocumentChunkRepository;
import com.mcs.rag.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int TOP_K = 5;
    private static final double MIN_SIMILARITY = 0.3;
    private static final int MAX_CONTEXT_CHARS = 3000;

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    public String search(String agentId, String query) {
        List<DocumentChunk> chunks = documentChunkRepository.findByAgentId(agentId);
        if (chunks.isEmpty()) return "";

        List<Double> queryEmbedding = embeddingService.embed(query);

        return chunks.stream()
                .map(chunk -> new ScoredChunk(chunk, embeddingService.cosineSimilarity(queryEmbedding, chunk.getEmbedding())))
                .filter(sc -> sc.score() >= MIN_SIMILARITY)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(TOP_K)
                .map(sc -> sc.chunk().getContent())
                .collect(Collectors.collectingAndThen(
                        Collectors.joining("\n\n"),
                        text -> text.length() > MAX_CONTEXT_CHARS ? text.substring(0, MAX_CONTEXT_CHARS) : text
                ));
    }

    private record ScoredChunk(DocumentChunk chunk, double score) {}
}
