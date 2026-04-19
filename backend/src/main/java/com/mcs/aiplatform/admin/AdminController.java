package com.mcs.aiplatform.admin;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.user.User;
import com.mcs.aiplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository        userRepository;
    private final AgentViewRepository   agentViewRepository;
    private final UsageLogViewRepository usageLogViewRepository;

    @GetMapping("/users")
    public ApiResponse<List<User>> users() {
        return ApiResponse.ok(userRepository.findAll());
    }

    @GetMapping("/agents")
    public ApiResponse<List<AgentView>> agents() {
        return ApiResponse.ok(agentViewRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/usage-logs")
    public ApiResponse<Map<String, Object>> usageLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<UsageLogView> result = usageLogViewRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return ApiResponse.ok(Map.of(
                "content",       result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages",    result.getTotalPages(),
                "page",          result.getNumber()
        ));
    }
}
