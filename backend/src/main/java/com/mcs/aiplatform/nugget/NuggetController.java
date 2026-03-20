package com.mcs.aiplatform.nugget;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/nuggets")
@RequiredArgsConstructor
public class NuggetController {

    private final NuggetService nuggetService;

    @GetMapping
    public ApiResponse<List<Nugget>> list(@PathVariable String agentId) {
        return ApiResponse.ok(nuggetService.list(CurrentUser.userId(), agentId));
    }

    @PostMapping
    public ApiResponse<Nugget> create(
            @PathVariable String agentId,
            @RequestBody CreateNuggetRequest req) {
        return ApiResponse.ok(nuggetService.create(CurrentUser.userId(), agentId, req));
    }

    @GetMapping("/{nuggetId}")
    public ApiResponse<Nugget> get(
            @PathVariable String agentId,
            @PathVariable String nuggetId) {
        return ApiResponse.ok(nuggetService.get(CurrentUser.userId(), agentId, nuggetId));
    }

    @PutMapping("/{nuggetId}")
    public ApiResponse<Nugget> update(
            @PathVariable String agentId,
            @PathVariable String nuggetId,
            @RequestBody UpdateNuggetRequest req) {
        return ApiResponse.ok(nuggetService.update(CurrentUser.userId(), agentId, nuggetId, req));
    }

    @DeleteMapping("/{nuggetId}")
    public ApiResponse<Void> delete(
            @PathVariable String agentId,
            @PathVariable String nuggetId) {
        nuggetService.delete(CurrentUser.userId(), agentId, nuggetId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{nuggetId}/status")
    public ApiResponse<Nugget> toggleStatus(
            @PathVariable String agentId,
            @PathVariable String nuggetId) {
        return ApiResponse.ok(nuggetService.toggleStatus(CurrentUser.userId(), agentId, nuggetId));
    }

    @PostMapping("/{nuggetId}/execute")
    public ApiResponse<String> execute(
            @PathVariable String agentId,
            @PathVariable String nuggetId,
            @RequestBody ExecuteNuggetRequest req) {
        return ApiResponse.ok(nuggetService.execute(CurrentUser.userId(), agentId, nuggetId, req.input()));
    }
}
