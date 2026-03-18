package com.mcs.aiplatform.template;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService service;

    @PostMapping
    public ApiResponse<PromptTemplate> create(@Valid @RequestBody CreateTemplateRequest req) {
        return ApiResponse.ok(service.create(CurrentUser.userId(), req));
    }

    @GetMapping
    public ApiResponse<List<PromptTemplate>> list() {
        return ApiResponse.ok(service.listByUser(CurrentUser.userId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PromptTemplate> get(@PathVariable String id) {
        return ApiResponse.ok(service.getOwned(id, CurrentUser.userId()));
    }

    @PostMapping("/{id}/render")
    public ApiResponse<String> render(@PathVariable String id, @RequestBody RenderTemplateRequest req) {
        return ApiResponse.ok(service.render(id, CurrentUser.userId(), req.variables()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id, CurrentUser.userId());
        return ApiResponse.ok(null);
    }
}
