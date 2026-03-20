package com.mcs.aiplatform.skill;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ApiResponse<List<Skill>> list(@PathVariable String agentId) {
        return ApiResponse.ok(skillService.list(CurrentUser.userId(), agentId));
    }

    @PostMapping
    public ApiResponse<Skill> create(
            @PathVariable String agentId,
            @RequestBody CreateSkillRequest req) {
        return ApiResponse.ok(skillService.create(CurrentUser.userId(), agentId, req));
    }

    @GetMapping("/{skillId}")
    public ApiResponse<Skill> get(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        return ApiResponse.ok(skillService.get(CurrentUser.userId(), agentId, skillId));
    }

    @PutMapping("/{skillId}")
    public ApiResponse<Skill> update(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @RequestBody UpdateSkillRequest req) {
        return ApiResponse.ok(skillService.update(CurrentUser.userId(), agentId, skillId, req));
    }

    @DeleteMapping("/{skillId}")
    public ApiResponse<Void> delete(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        skillService.delete(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{skillId}/status")
    public ApiResponse<Skill> toggleStatus(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        return ApiResponse.ok(skillService.toggleStatus(CurrentUser.userId(), agentId, skillId));
    }

    @PostMapping("/{skillId}/execute")
    public ApiResponse<String> execute(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @RequestBody ExecuteSkillRequest req) {
        return ApiResponse.ok(skillService.execute(CurrentUser.userId(), agentId, skillId, req.input()));
    }
}
