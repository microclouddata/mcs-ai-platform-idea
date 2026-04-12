package com.mcs.chat.memory;

import com.mcs.chat.chat.ChatMessage;
import com.mcs.chat.chat.ChatMessageRepository;
import com.mcs.chat.llm.LlmProviderFactory;
import com.mcs.chat.llm.LlmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private static final int SHORT_TERM_LIMIT = 10;

    private final ChatMessageRepository messageRepository;
    private final LlmProviderFactory llmProviderFactory;

    public String buildShortTermContext(String sessionId) {
        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (history.isEmpty()) return "";
        List<ChatMessage> recent = history.size() > SHORT_TERM_LIMIT
                ? history.subList(history.size() - SHORT_TERM_LIMIT, history.size())
                : history;
        return recent.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    public String summarize(String sessionId, String provider) {
        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (history.size() < 20) return null;
        String conversation = history.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
        try {
            return llmProviderFactory.get(provider)
                    .chat(new LlmRequest("Summarize the following conversation in 3-5 sentences.", conversation))
                    .content();
        } catch (Exception e) {
            log.warn("Summarization failed for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }
}
