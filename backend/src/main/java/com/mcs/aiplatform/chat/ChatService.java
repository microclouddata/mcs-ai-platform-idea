package com.mcs.aiplatform.chat;

import com.mcs.aiplatform.agent.Agent;
import com.mcs.aiplatform.agent.AgentService;
import com.mcs.aiplatform.llm.LlmProviderFactory;
import com.mcs.aiplatform.llm.LlmRequest;
import com.mcs.aiplatform.memory.MemoryService;
import com.mcs.aiplatform.tool.KnowledgeSearchTool;
import com.mcs.aiplatform.tool.ToolResult;
import com.mcs.aiplatform.tool.ToolService;
import com.mcs.aiplatform.usage.UsageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AgentService agentService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KnowledgeSearchTool knowledgeSearchTool;
    private final ToolService toolService;
    private final MemoryService memoryService;
    private final UsageLogService usageLogService;
    private final LlmProviderFactory llmProviderFactory;

    public ChatResponse chat(String userId, ChatRequest request) {
        Agent agent = agentService.get(userId, request.agentId());
        ChatSession session = resolveSession(userId, request.agentId(), request.sessionId(), request.message());

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setAgentId(agent.getId());
        userMessage.setUserId(userId);
        userMessage.setRole("USER");
        userMessage.setContent(request.message());
        chatMessageRepository.save(userMessage);

        // Tool execution
        String toolContext;
        if (agent.isToolsEnabled()) {
            String toolOutput = toolService.executeAll(request.message(), agent.getId());
            // Fall back to legacy KnowledgeSearchTool if no bindings configured
            if (toolOutput.isBlank()) {
                ToolResult legacyResult = knowledgeSearchTool.search(agent.getId(), request.message());
                toolContext = legacyResult.content();
            } else {
                toolContext = toolOutput;
            }
        } else {
            toolContext = "";
        }

        // Memory context
        String memoryContext = agent.isMemoryEnabled()
                ? memoryService.buildShortTermContext(session.getId())
                : "";

        // Build prompt
        StringBuilder prompt = new StringBuilder("Question:\n").append(request.message());
        if (!toolContext.isBlank()) {
            prompt.append("\n\nKnowledge Context:\n").append(toolContext);
        }
        if (!memoryContext.isBlank()) {
            prompt.append("\n\nConversation History:\n").append(memoryContext);
        }

        String answer = llmProviderFactory.get(agent.getProvider())
                .chat(new LlmRequest(agent.getSystemPrompt(), prompt.toString()))
                .content();

        // Persist assistant message
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setAgentId(agent.getId());
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("ASSISTANT");
        assistantMessage.setContent(answer);
        if (!toolContext.isBlank()) {
            assistantMessage.setToolResults(List.of(new ToolResult("tools", toolContext)));
        }
        chatMessageRepository.save(assistantMessage);

        // Usage logging (token counts estimated; replace with actual counts from LLM response if available)
        int promptTokens = prompt.length() / 4;
        int completionTokens = answer.length() / 4;
        usageLogService.record(userId, agent.getId(), session.getId(),
                agent.getProvider(), agent.getModel(), promptTokens, completionTokens);

        return new ChatResponse(session.getId(), answer);
    }

    public List<ChatMessage> history(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private ChatSession resolveSession(String userId, String agentId, String sessionId, String firstMessage) {
        if (sessionId != null && !sessionId.isBlank()) {
            return chatSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));
        }
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setAgentId(agentId);
        session.setTitle(firstMessage.length() > 60 ? firstMessage.substring(0, 60) : firstMessage);
        return chatSessionRepository.save(session);
    }
}