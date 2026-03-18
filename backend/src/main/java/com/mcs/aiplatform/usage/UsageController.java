package com.mcs.aiplatform.usage;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
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

    @GetMapping("/stats")
    public ApiResponse<UsageStatsResponse> stats() {
        return ApiResponse.ok(usageLogService.statsForUser(CurrentUser.userId()));
    }

    @GetMapping("/logs")
    public ApiResponse<List<UsageLog>> logs() {
        return ApiResponse.ok(usageLogService.getByUser(CurrentUser.userId()));
    }
}
