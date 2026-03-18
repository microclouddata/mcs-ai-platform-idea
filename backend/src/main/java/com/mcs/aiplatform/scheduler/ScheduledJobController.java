package com.mcs.aiplatform.scheduler;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class ScheduledJobController {

    private final ScheduledJobService service;

    @PostMapping
    public ApiResponse<ScheduledJob> create(@Valid @RequestBody CreateScheduledJobRequest req) {
        return ApiResponse.ok(service.create(CurrentUser.userId(), req));
    }

    @GetMapping
    public ApiResponse<List<ScheduledJob>> list() {
        return ApiResponse.ok(service.listByUser(CurrentUser.userId()));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<ScheduledJob> toggleEnabled(@PathVariable String id) {
        return ApiResponse.ok(service.toggleEnabled(id, CurrentUser.userId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id, CurrentUser.userId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/schedule-types")
    public ApiResponse<List<String>> scheduleTypes() {
        return ApiResponse.ok(Arrays.stream(ScheduleType.values()).map(Enum::name).toList());
    }
}
