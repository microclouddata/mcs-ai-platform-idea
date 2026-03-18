package com.mcs.aiplatform.audit;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    public ApiResponse<List<AuditLog>> myLogs() {
        return ApiResponse.ok(auditService.listForUser(CurrentUser.userId()));
    }

    @GetMapping("/logs/org/{orgId}")
    public ApiResponse<List<AuditLog>> orgLogs(@PathVariable String orgId) {
        return ApiResponse.ok(auditService.listForOrg(orgId));
    }
}
