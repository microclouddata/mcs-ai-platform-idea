package com.mcs.aiplatform.skill;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends MongoRepository<Skill, String> {

    List<Skill> findByAgentId(String agentId);

    List<Skill> findByAgentIdAndStatus(String agentId, SkillStatus status);

    Optional<Skill> findByIdAndAgentId(String id, String agentId);

    Optional<Skill> findByAgentIdAndNameIgnoreCase(String agentId, String name);

    void deleteByAgentId(String agentId);
}
