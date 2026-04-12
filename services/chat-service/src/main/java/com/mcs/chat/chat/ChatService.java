package com.mcs.chat.chat;

import com.mcs.chat.client.AgentServiceClient;
import com.mcs.chat.client.AgentServiceClient.AgentDto;
import com.mcs.chat.client.AgentServiceClient.SkillDto;
import com.mcs.chat.client.RagServiceClient;
import com.mcs.chat.kafka.event.UsageRecordedEvent;
import com.mcs.chat.kafka.producer.UsageEventProducer;
import com.mcs.chat.llm.LlmProviderFactory;
import com.mcs.chat.llm.LlmRequest;
import com.mcs.chat.memory.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Pattern CALL_SKILL_PATTERN =
            Pattern.compile("^call\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemoryService memoryService;
    private final UsageEventProducer usageEventProducer;
    private final LlmProviderFactory llmProviderFactory;
    private final AgentServiceClient agentServiceClient;
    private final RagServiceClient ragServiceClient;

    public ChatResponse chat(String userId, ChatRequest request) {
        AgentDto agent = agentServiceClient.getAgent(request.agentId());
        if (!agent.isEnabled() && !agent.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Agent not found");
        }

        ChatSession session = resolveSession(userId, request.agentId(), request.sessionId(), request.message());

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setAgentId(agent.getId());
        userMessage.setUserId(userId);
        userMessage.setRole("USER");
        userMessage.setContent(request.message());
        chatMessageRepository.save(userMessage);

        // "call <skill name>" — execute named skill directly
        Matcher callMatcher = CALL_SKILL_PATTERN.matcher(request.message().trim());
        if (callMatcher.matches()) {
            String skillName = callMatcher.group(1).trim();
            String skillResult;
            try {
                SkillDto skill = agentServiceClient.getSkillByName(agent.getId(), skillName);
                skillResult = skill.getInstructions() != null ? skill.getInstructions()
                        : "(No instructions defined for skill: " + skillName + ")";
            } catch (Exception e) {
                skillResult = "Skill not found: " + skillName;
            }
            ChatMessage skillResponse = new ChatMessage();
            skillResponse.setSessionId(session.getId());
            skillResponse.setAgentId(agent.getId());
            skillResponse.setUserId(userId);
            skillResponse.setRole("ASSISTANT");
            skillResponse.setContent(skillResult);
            chatMessageRepository.save(skillResponse);
            return new ChatResponse(session.getId(), skillResult);
        }

        // Knowledge retrieval from rag-service
        String toolContext = "";
        if (agent.isToolsEnabled()) {
            toolContext = ragServiceClient.search(agent.getId(), request.message());
        }

        // Skill context from agent-service
        String skillContext = resolveSkillContext(agent.getId(), request.skillId(), request.message());
        if (!skillContext.isBlank()) {
            toolContext = toolContext.isBlank() ? skillContext : toolContext + "\n\n" + skillContext;
        }

        // Short-term memory context
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

        String systemPrompt = agent.getSystemPrompt() != null ? agent.getSystemPrompt()
                : "You are a helpful assistant.";

        String answer = llmProviderFactory.get(agent.getProvider())
                .chat(new LlmRequest(systemPrompt, prompt.toString()))
                .content();

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setAgentId(agent.getId());
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("ASSISTANT");
        assistantMessage.setContent(answer);
        chatMessageRepository.save(assistantMessage);

        int promptTokens = prompt.length() / 4;
        int completionTokens = answer.length() / 4;
        usageEventProducer.publishUsageRecorded(new UsageRecordedEvent(
                userId, agent.getId(), session.getId(),
                agent.getProvider(), agent.getModel(),
                promptTokens, completionTokens
        ));

        return new ChatResponse(session.getId(), answer);
    }

    public List<ChatMessage> history(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private String resolveSkillContext(String agentId, String skillId, String message) {
        if ("__none__".equals(skillId)) return "";

        if (skillId != null && !skillId.isBlank()) {
            try {
                SkillDto skill = agentServiceClient.getSkillById(agentId, skillId);
                if (skill.getInstructions() != null && !skill.getInstructions().isBlank()) {
                    return "[SKILL: " + skill.getName() + "]\n" + skill.getInstructions();
                }
            } catch (Exception ignored) {}
            return "";
        }

        try {
            List<SkillDto> activeSkills = agentServiceClient.getActiveSkills(agentId);
            return activeSkills.stream()
                    .filter(s -> "ACTIVE".equals(s.getStatus()))
                    .filter(s -> s.getInstructions() != null && !s.getInstructions().isBlank())
                    .map(s -> "[SKILL: " + s.getName() + "]\n" + s.getInstructions())
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception ignored) {
            return "";
        }
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
