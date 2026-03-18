package com.mcs.aiplatform.billing;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/subscription")
    public ApiResponse<Subscription> get() {
        return ApiResponse.ok(subscriptionService.getOrCreate(CurrentUser.userId()));
    }

    @PostMapping("/subscription/upgrade")
    public ApiResponse<Subscription> upgrade(@RequestBody UpgradePlanRequest req) {
        return ApiResponse.ok(subscriptionService.upgrade(CurrentUser.userId(), req.plan()));
    }
}
