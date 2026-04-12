package com.mcs.agent.skill;

import com.mcs.agent.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ApiResponse<Skill> create(@PathVariable String agentId,
                                     @RequestBody CreateSkillRequest request) {
        return ApiResponse.ok(skillService.create(agentId, request));
    }

    @GetMapping
    public ApiResponse<List<Skill>> list(@PathVariable String agentId) {
        return ApiResponse.ok(skillService.listByAgent(agentId));
    }

    @GetMapping("/{skillId}")
    public ApiResponse<Skill> get(@PathVariable String agentId,
                                  @PathVariable String skillId) {
        return ApiResponse.ok(skillService.getById(skillId));
    }

    @PutMapping("/{skillId}")
    public ApiResponse<Skill> update(@PathVariable String agentId,
                                     @PathVariable String skillId,
                                     @RequestBody UpdateSkillRequest request) {
        return ApiResponse.ok(skillService.update(skillId, request));
    }

    @DeleteMapping("/{skillId}")
    public ApiResponse<Void> delete(@PathVariable String agentId,
                                    @PathVariable String skillId) {
        skillService.delete(skillId);
        return ApiResponse.ok(null);
    }
}
