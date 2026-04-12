package com.mcs.agent.skill;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public Skill create(String agentId, CreateSkillRequest request) {
        Skill skill = new Skill();
        skill.setAgentId(agentId);
        skill.setName(request.name());
        skill.setDescription(request.description());
        skill.setInstructions(request.instructions());
        skill.setLicense(request.license());
        skill.setCompatibility(request.compatibility());
        if (request.allowedTools() != null) skill.setAllowedTools(request.allowedTools());
        if (request.skillMetadata() != null) skill.setSkillMetadata(request.skillMetadata());
        if (request.status() != null) skill.setStatus(request.status());
        return skillRepository.save(skill);
    }

    @Cacheable(value = "skills", key = "#agentId")
    public List<Skill> listByAgent(String agentId) {
        return skillRepository.findByAgentId(agentId);
    }

    @Cacheable(value = "skills", key = "#agentId + ':active'")
    public List<Skill> listActive(String agentId) {
        return skillRepository.findByAgentIdAndStatus(agentId, SkillStatus.ACTIVE);
    }

    public Skill getById(String skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));
    }

    public Skill getByName(String agentId, String name) {
        return skillRepository.findByAgentIdAndNameIgnoreCase(agentId, name)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + name));
    }

    @CacheEvict(value = "skills", allEntries = true)
    public Skill update(String skillId, UpdateSkillRequest request) {
        Skill skill = getById(skillId);
        if (request.name() != null) skill.setName(request.name());
        if (request.description() != null) skill.setDescription(request.description());
        if (request.instructions() != null) skill.setInstructions(request.instructions());
        if (request.license() != null) skill.setLicense(request.license());
        if (request.compatibility() != null) skill.setCompatibility(request.compatibility());
        if (request.allowedTools() != null) skill.setAllowedTools(request.allowedTools());
        if (request.skillMetadata() != null) skill.setSkillMetadata(request.skillMetadata());
        if (request.status() != null) skill.setStatus(request.status());
        return skillRepository.save(skill);
    }

    @CacheEvict(value = "skills", allEntries = true)
    public void delete(String skillId) {
        Skill skill = getById(skillId);
        skillRepository.delete(skill);
    }
}
