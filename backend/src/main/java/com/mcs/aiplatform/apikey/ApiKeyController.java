package com.mcs.aiplatform.apikey;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public ApiResponse<List<ApiKeyResponse>> list() {
        return ApiResponse.ok(apiKeyService.listForUser(CurrentUser.userId()));
    }

    @PostMapping
    public ApiResponse<ApiKeyResponse> create(@RequestBody CreateApiKeyRequest req) {
        return ApiResponse.ok(apiKeyService.create(CurrentUser.userId(), req));
    }

    @DeleteMapping("/{keyId}")
    public ApiResponse<Void> revoke(@PathVariable String keyId) {
        apiKeyService.revoke(keyId, CurrentUser.userId());
        return ApiResponse.ok(null);
    }
}
