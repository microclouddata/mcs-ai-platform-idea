package com.mcs.aiplatform.integration;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping
    public ApiResponse<List<Integration>> list() {
        return ApiResponse.ok(integrationService.listForUser(CurrentUser.userId()));
    }

    @PostMapping
    public ApiResponse<Integration> create(@RequestBody CreateIntegrationRequest req) {
        return ApiResponse.ok(integrationService.create(CurrentUser.userId(), req));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<Void> toggle(@PathVariable String id) {
        integrationService.toggleEnabled(id, CurrentUser.userId());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        integrationService.delete(id, CurrentUser.userId());
        return ApiResponse.ok(null);
    }
}
