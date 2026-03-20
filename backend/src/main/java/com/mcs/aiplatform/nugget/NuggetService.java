package com.mcs.aiplatform.nugget;

import com.mcs.aiplatform.agent.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NuggetService {

    private final NuggetRepository nuggetRepository;
    private final AgentService agentService;
    private final NuggetExecutor nuggetExecutor;

    public List<Nugget> list(String userId, String agentId) {
        validateOwner(userId, agentId);
        return nuggetRepository.findByAgentId(agentId);
    }

    public Nugget get(String userId, String agentId, String nuggetId) {
        validateOwner(userId, agentId);
        return nuggetRepository.findByIdAndAgentId(nuggetId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("Nugget not found"));
    }

    public Nugget create(String userId, String agentId, CreateNuggetRequest req) {
        validateOwner(userId, agentId);
        Nugget nugget = new Nugget();
        nugget.setAgentId(agentId);
        applyRequest(nugget, req.name(), req.description(), req.code(), req.language(),
                req.status(), req.nuggetType(), req.docId(), req.controlFlags(),
                req.metadata(), req.tags(), req.parameters(), req.modelTool());
        return nuggetRepository.save(nugget);
    }

    public Nugget update(String userId, String agentId, String nuggetId, UpdateNuggetRequest req) {
        Nugget nugget = get(userId, agentId, nuggetId);
        applyRequest(nugget, req.name(), req.description(), req.code(), req.language(),
                req.status(), req.nuggetType(), req.docId(), req.controlFlags(),
                req.metadata(), req.tags(), req.parameters(), req.modelTool());
        return nuggetRepository.save(nugget);
    }

    public void delete(String userId, String agentId, String nuggetId) {
        Nugget nugget = get(userId, agentId, nuggetId);
        nuggetRepository.deleteById(nugget.getId());
    }

    public Nugget toggleStatus(String userId, String agentId, String nuggetId) {
        Nugget nugget = get(userId, agentId, nuggetId);
        nugget.setStatus(nugget.getStatus() == NuggetStatus.ACTIVE ? NuggetStatus.INACTIVE : NuggetStatus.ACTIVE);
        return nuggetRepository.save(nugget);
    }

    public String execute(String userId, String agentId, String nuggetId, String input) {
        Nugget nugget = get(userId, agentId, nuggetId);
        return nuggetExecutor.execute(nugget, input);
    }

    /**
     * Called by ChatService — runs all ACTIVE nuggets for an agent and concatenates results.
     */
    public String executeActiveNuggets(String agentId, String input) {
        List<Nugget> active = nuggetRepository.findByAgentIdAndStatus(agentId, NuggetStatus.ACTIVE);
        if (active.isEmpty()) return "";

        StringBuilder results = new StringBuilder();
        for (Nugget nugget : active) {
            try {
                String result = nuggetExecutor.execute(nugget, input);
                if (result != null && !result.isBlank()) {
                    results.append("[NUGGET: ").append(nugget.getName()).append("]\n")
                            .append(result).append("\n\n");
                }
            } catch (Exception e) {
                log.warn("Nugget '{}' failed for agent {}: {}", nugget.getName(), agentId, e.getMessage());
            }
        }
        return results.toString().trim();
    }

    private void validateOwner(String userId, String agentId) {
        agentService.get(userId, agentId);
    }

    private void applyRequest(Nugget nugget, String name, String description, String code,
                              NuggetLanguage language, NuggetStatus status, NuggetType nuggetType,
                              String docId, List<String> controlFlags,
                              java.util.Map<String, String> metadata, List<String> tags,
                              List<NuggetParameter> parameters, Boolean modelTool) {
        if (name != null) nugget.setName(name);
        if (description != null) nugget.setDescription(description);
        if (code != null) nugget.setCode(code);
        if (language != null) nugget.setLanguage(language);
        if (status != null) nugget.setStatus(status);
        if (nuggetType != null) nugget.setNuggetType(nuggetType);
        if (docId != null) nugget.setDocId(docId);
        if (controlFlags != null) nugget.setControlFlags(new ArrayList<>(controlFlags));
        if (metadata != null) nugget.setMetadata(new LinkedHashMap<>(metadata));
        if (tags != null) nugget.setTags(new ArrayList<>(tags));
        if (parameters != null) nugget.setParameters(new ArrayList<>(parameters));
        if (modelTool != null) nugget.setModelTool(modelTool);
    }
}
