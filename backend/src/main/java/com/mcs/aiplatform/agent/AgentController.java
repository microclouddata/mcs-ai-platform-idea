package com.mcs.aiplatform.agent;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *  agent controller 2
 */
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    public ApiResponse<Agent> create(@RequestBody CreateAgentRequest request) {
        return ApiResponse.ok(agentService.create(CurrentUser.userId(), request));
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(agentService.list(CurrentUser.userId()));
    }

    @GetMapping("/{agentId}")
    public ApiResponse<Agent> get(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.getForUse(agentId, CurrentUser.userId()));
    }

    @PutMapping("/{agentId}")
    public ApiResponse<Agent> update(@PathVariable String agentId,
                                     @RequestBody UpdateAgentRequest request) {
        return ApiResponse.ok(agentService.update(CurrentUser.userId(), agentId, request));
    }

    @PatchMapping("/{agentId}/enabled")
    public ApiResponse<Agent> toggleEnabled(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.toggleEnabled(CurrentUser.userId(), agentId));
    }

    @DeleteMapping("/{agentId}")
    public ApiResponse<Void> delete(@PathVariable String agentId) {
        agentService.delete(CurrentUser.userId(), agentId);
        return ApiResponse.ok(null);
    }
}
