package com.mcs.agent.internal;

import com.mcs.agent.agent.Agent;
import com.mcs.agent.agent.AgentRepository;
import com.mcs.agent.skill.Skill;
import com.mcs.agent.skill.SkillService;
import com.mcs.agent.skill.SkillStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API for inter-service communication.
 * These endpoints are permit-all (no JWT required) and must not be exposed externally.
 * The API Gateway routes /internal/** to no external path.
 */
@RestController
@RequestMapping("/internal/agents")
@RequiredArgsConstructor
public class AgentInternalController {

    private final AgentRepository agentRepository;
    private final SkillService skillService;

    @GetMapping("/{agentId}")
    public Agent getAgent(@PathVariable String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
    }

    @GetMapping("/{agentId}/skills/active")
    public List<Skill> getActiveSkills(@PathVariable String agentId) {
        return skillService.listActive(agentId);
    }

    @GetMapping("/{agentId}/skills/{skillId}")
    public Skill getSkillById(@PathVariable String agentId, @PathVariable String skillId) {
        Skill skill = skillService.getById(skillId);
        if (!agentId.equals(skill.getAgentId())) {
            throw new IllegalArgumentException("Skill not found for agent: " + skillId);
        }
        return skill;
    }

    @GetMapping("/{agentId}/skills/by-name/{name}")
    public Skill getSkillByName(@PathVariable String agentId, @PathVariable String name) {
        return skillService.getByName(agentId, name);
    }
}
