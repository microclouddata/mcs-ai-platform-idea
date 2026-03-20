package com.mcs.aiplatform.skill;

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
public class SkillService {

    private final SkillRepository skillRepository;
    private final AgentService agentService;
    private final SkillExecutor skillExecutor;

    public List<Skill> list(String userId, String agentId) {
        validateOwner(userId, agentId);
        return skillRepository.findByAgentId(agentId);
    }

    public Skill get(String userId, String agentId, String skillId) {
        validateOwner(userId, agentId);
        return skillRepository.findByIdAndAgentId(skillId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
    }

    public Skill create(String userId, String agentId, CreateSkillRequest req) {
        validateOwner(userId, agentId);
        Skill skill = new Skill();
        skill.setAgentId(agentId);
        applyRequest(skill, req.name(), req.description(), req.code(), req.language(),
                req.status(), req.skillType(), req.docId(), req.controlFlags(),
                req.metadata(), req.tags(), req.parameters(), req.modelTool());
        return skillRepository.save(skill);
    }

    public Skill update(String userId, String agentId, String skillId, UpdateSkillRequest req) {
        Skill skill = get(userId, agentId, skillId);
        applyRequest(skill, req.name(), req.description(), req.code(), req.language(),
                req.status(), req.skillType(), req.docId(), req.controlFlags(),
                req.metadata(), req.tags(), req.parameters(), req.modelTool());
        return skillRepository.save(skill);
    }

    public void delete(String userId, String agentId, String skillId) {
        Skill skill = get(userId, agentId, skillId);
        skillRepository.deleteById(skill.getId());
    }

    public Skill toggleStatus(String userId, String agentId, String skillId) {
        Skill skill = get(userId, agentId, skillId);
        skill.setStatus(skill.getStatus() == SkillStatus.ACTIVE ? SkillStatus.INACTIVE : SkillStatus.ACTIVE);
        return skillRepository.save(skill);
    }

    public String execute(String userId, String agentId, String skillId, String input) {
        Skill skill = get(userId, agentId, skillId);
        return skillExecutor.execute(skill, input);
    }

    /**
     * Called by ChatService when the user types "call <skill name>" in chat.
     * Looks up the skill by name (case-insensitive) and executes it directly.
     */
    public String executeByName(String agentId, String skillName, String input) {
        Skill skill = skillRepository.findByAgentIdAndNameIgnoreCase(agentId, skillName.trim())
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillName.trim()));
        return skillExecutor.execute(skill, input);
    }

    /**
     * Called by ChatService — runs all ACTIVE skills for an agent and concatenates results.
     */
    public String executeActiveSkills(String agentId, String input) {
        List<Skill> active = skillRepository.findByAgentIdAndStatus(agentId, SkillStatus.ACTIVE);
        if (active.isEmpty()) return "";

        StringBuilder results = new StringBuilder();
        for (Skill skill : active) {
            try {
                String result = skillExecutor.execute(skill, input);
                if (result != null && !result.isBlank()) {
                    results.append("[SKILL: ").append(skill.getName()).append("]\n")
                            .append(result).append("\n\n");
                }
            } catch (Exception e) {
                log.warn("Skill '{}' failed for agent {}: {}", skill.getName(), agentId, e.getMessage());
            }
        }
        return results.toString().trim();
    }

    private void validateOwner(String userId, String agentId) {
        agentService.get(userId, agentId);
    }

    private void applyRequest(Skill skill, String name, String description, String code,
                              SkillLanguage language, SkillStatus status, SkillType skillType,
                              String docId, List<String> controlFlags,
                              java.util.Map<String, String> metadata, List<String> tags,
                              List<SkillParameter> parameters, Boolean modelTool) {
        if (name != null) skill.setName(name);
        if (description != null) skill.setDescription(description);
        if (code != null) skill.setCode(code);
        if (language != null) skill.setLanguage(language);
        if (status != null) skill.setStatus(status);
        if (skillType != null) skill.setSkillType(skillType);
        if (docId != null) skill.setDocId(docId);
        if (controlFlags != null) skill.setControlFlags(new ArrayList<>(controlFlags));
        if (metadata != null) skill.setMetadata(new LinkedHashMap<>(metadata));
        if (tags != null) skill.setTags(new ArrayList<>(tags));
        if (parameters != null) skill.setParameters(new ArrayList<>(parameters));
        if (modelTool != null) skill.setModelTool(modelTool);
    }
}
