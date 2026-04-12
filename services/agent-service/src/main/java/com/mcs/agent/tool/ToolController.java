package com.mcs.agent.tool;

import com.mcs.agent.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @GetMapping
    public ApiResponse<List<AgentToolBinding>> getBindings(@PathVariable String agentId) {
        return ApiResponse.ok(toolService.getAllBindings(agentId));
    }

    @PutMapping
    public ApiResponse<List<AgentToolBinding>> updateBindings(
            @PathVariable String agentId,
            @RequestBody List<String> toolTypeNames) {
        return ApiResponse.ok(toolService.updateBindings(agentId, toolTypeNames));
    }

    @GetMapping("/available")
    public ApiResponse<List<String>> availableTools() {
        return ApiResponse.ok(toolService.availableToolTypes());
    }
}
