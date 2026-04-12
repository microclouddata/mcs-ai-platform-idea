package com.mcs.usage.usage;

import com.mcs.usage.common.ApiResponse;
import com.mcs.usage.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageLogService usageLogService;

    /**
     * GET /api/usage/stats
     * Returns aggregated usage statistics for the authenticated user.
     */
    @GetMapping("/stats")
    public ApiResponse<UsageStatsResponse> getStats() {
        String userId = CurrentUser.userId();
        UsageStatsResponse stats = usageLogService.statsForUser(userId);
        return ApiResponse.ok(stats);
    }

    /**
     * GET /api/usage/logs
     * Returns all usage log entries for the authenticated user.
     */
    @GetMapping("/logs")
    public ApiResponse<List<UsageLog>> getLogs() {
        String userId = CurrentUser.userId();
        List<UsageLog> logs = usageLogService.getByUser(userId);
        return ApiResponse.ok(logs);
    }
}
