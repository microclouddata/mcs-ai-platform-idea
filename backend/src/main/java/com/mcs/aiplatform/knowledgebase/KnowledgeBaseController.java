package com.mcs.aiplatform.knowledgebase;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService service;

    @PostMapping
    public ApiResponse<KnowledgeBase> create(@Valid @RequestBody CreateKnowledgeBaseRequest req) {
        return ApiResponse.ok(service.create(CurrentUser.userId(), req));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBase>> list() {
        return ApiResponse.ok(service.listByUser(CurrentUser.userId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBase> get(@PathVariable String id) {
        return ApiResponse.ok(service.getByIdAndUser(id, CurrentUser.userId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id, CurrentUser.userId());
        return ApiResponse.ok(null);
    }
}