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
    private final SkillFileService skillFileService;

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
        applyRequest(skill, req);
        Skill saved = skillRepository.save(skill);
        skillFileService.initSkillDirectory(saved.getId());
        return saved;
    }

    public Skill update(String userId, String agentId, String skillId, UpdateSkillRequest req) {
        Skill skill = get(userId, agentId, skillId);
        applyUpdateRequest(skill, req);
        return skillRepository.save(skill);
    }

    public void delete(String userId, String agentId, String skillId) {
        Skill skill = get(userId, agentId, skillId);
        skillRepository.deleteById(skill.getId());
        skillFileService.deleteSkillDirectory(skill.getId());
    }

    public Skill toggleStatus(String userId, String agentId, String skillId) {
        Skill skill = get(userId, agentId, skillId);
        skill.setStatus(skill.getStatus() == SkillStatus.ACTIVE ? SkillStatus.INACTIVE : SkillStatus.ACTIVE);
        return skillRepository.save(skill);
    }

    /**
     * Called by ChatService when the user selects a specific skill in the chat UI.
     */
    public String executeById(String agentId, String skillId) {
        Skill skill = skillRepository.findByIdAndAgentId(skillId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));
        if (skill.getStatus() != SkillStatus.ACTIVE) return "";
        String instructions = skill.getInstructions();
        return (instructions != null && !instructions.isBlank())
                ? "[SKILL: " + skill.getName() + "]\n" + instructions
                : "";
    }

    /**
     * Called by ChatService when the user types "call <skill name>" in chat.
     * Returns the skill's instructions as context.
     */
    public String executeByName(String agentId, String skillName, String input) {
        Skill skill = skillRepository.findByAgentIdAndNameIgnoreCase(agentId, skillName.trim())
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillName.trim()));
        String instructions = skill.getInstructions();
        return (instructions != null && !instructions.isBlank()) ? instructions : "(No instructions defined for skill: " + skill.getName() + ")";
    }

    /**
     * Called by ChatService — returns instructions of all ACTIVE skills as LLM context.
     * Metadata (name, description) is already available from the MongoDB document.
     * Instructions are the SKILL.md body, loaded here on activation.
     */
    public String executeActiveSkills(String agentId, String input) {
        List<Skill> active = skillRepository.findByAgentIdAndStatus(agentId, SkillStatus.ACTIVE);
        if (active.isEmpty()) return "";

        StringBuilder results = new StringBuilder();
        for (Skill skill : active) {
            String instructions = skill.getInstructions();
            if (instructions != null && !instructions.isBlank()) {
                results.append("[SKILL: ").append(skill.getName()).append("]\n")
                        .append(instructions).append("\n\n");
            }
        }
        return results.toString().trim();
    }

    private void validateOwner(String userId, String agentId) {
        agentService.get(userId, agentId);
    }

    private void applyRequest(Skill skill, CreateSkillRequest req) {
        if (req.name() != null) skill.setName(req.name());
        if (req.description() != null) skill.setDescription(req.description());
        if (req.license() != null) skill.setLicense(req.license());
        if (req.compatibility() != null) skill.setCompatibility(req.compatibility());
        if (req.skillMetadata() != null) skill.setSkillMetadata(new LinkedHashMap<>(req.skillMetadata()));
        if (req.allowedTools() != null) skill.setAllowedTools(new ArrayList<>(req.allowedTools()));
        if (req.instructions() != null) skill.setInstructions(req.instructions());
        if (req.status() != null) skill.setStatus(req.status());
        if (req.modelTool() != null) skill.setModelTool(req.modelTool());
    }

    private void applyUpdateRequest(Skill skill, UpdateSkillRequest req) {
        if (req.name() != null) skill.setName(req.name());
        if (req.description() != null) skill.setDescription(req.description());
        if (req.license() != null) skill.setLicense(req.license());
        if (req.compatibility() != null) skill.setCompatibility(req.compatibility());
        if (req.skillMetadata() != null) skill.setSkillMetadata(new LinkedHashMap<>(req.skillMetadata()));
        if (req.allowedTools() != null) skill.setAllowedTools(new ArrayList<>(req.allowedTools()));
        if (req.instructions() != null) skill.setInstructions(req.instructions());
        if (req.status() != null) skill.setStatus(req.status());
        if (req.modelTool() != null) skill.setModelTool(req.modelTool());
    }
}
