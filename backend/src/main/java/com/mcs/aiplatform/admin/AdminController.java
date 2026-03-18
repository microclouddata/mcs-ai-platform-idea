package com.mcs.aiplatform.admin;

import com.mcs.aiplatform.agent.Agent;
import com.mcs.aiplatform.agent.AgentRepository;
import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.usage.UsageLog;
import com.mcs.aiplatform.usage.UsageLogService;
import com.mcs.aiplatform.user.User;
import com.mcs.aiplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final UsageLogService usageLogService;

    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.ok(userRepository.findAll());
    }

    @GetMapping("/agents")
    public ApiResponse<List<Agent>> listAgents() {
        return ApiResponse.ok(agentRepository.findAll());
    }

    @GetMapping("/usage-logs")
    public ApiResponse<Page<UsageLog>> usageLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(usageLogService.getAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}