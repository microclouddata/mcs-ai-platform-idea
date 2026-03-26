package com.mcs.aiplatform.skill;

import java.util.List;
import java.util.Map;

/**
 * Request to create a skill following the agentskills.md specification.
 *
 * @param name         Skill identifier — max 64 chars, lowercase letters, numbers, hyphens only
 * @param description  When to trigger and what the skill does — max 1024 chars
 * @param license      Optional license name or reference (e.g. "Apache-2.0")
 * @param compatibility Optional environment requirements (e.g. "Requires Python 3.10+")
 * @param skillMetadata Optional free-form key-value metadata (maps to frontmatter 'metadata')
 * @param allowedTools  Optional list of pre-approved tools the skill may use
 * @param instructions  Body content of SKILL.md — the LLM instructions
 * @param status       ACTIVE or INACTIVE (default: ACTIVE)
 * @param modelTool    Whether this skill is exposed as a model tool
 */
public record CreateSkillRequest(
        String name,
        String description,
        String license,
        String compatibility,
        Map<String, String> skillMetadata,
        List<String> allowedTools,
        String instructions,
        SkillStatus status,
        Boolean modelTool
) {
}
