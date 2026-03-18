package com.mcs.aiplatform.tool;

import com.mcs.aiplatform.tool.executor.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {

    private final AgentToolBindingRepository bindingRepository;
    private final List<ToolExecutor> executors;

    public List<AgentToolBinding> getEnabledBindings(String agentId) {
        return bindingRepository.findByAgentIdAndEnabled(agentId, true);
    }

    public List<AgentToolBinding> getAllBindings(String agentId) {
        return bindingRepository.findByAgentId(agentId);
    }

    /**
     * Executes all enabled tools for an agent and concatenates their results.
     */
    public String executeAll(String query, String agentId) {
        List<AgentToolBinding> bindings = getEnabledBindings(agentId);
        if (bindings.isEmpty()) return "";

        Map<ToolType, ToolExecutor> executorMap = executors.stream()
                .collect(Collectors.toMap(ToolExecutor::supportedType, Function.identity()));

        StringBuilder results = new StringBuilder();
        for (AgentToolBinding binding : bindings) {
            ToolExecutor executor = executorMap.get(binding.getToolType());
            if (executor == null) continue;
            try {
                String result = executor.execute(query, binding, agentId);
                if (result != null && !result.isBlank()) {
                    results.append("[").append(binding.getToolType().name()).append("]\n")
                            .append(result).append("\n\n");
                }
            } catch (Exception e) {
                log.warn("Tool {} failed for agent {}: {}", binding.getToolType(), agentId, e.getMessage());
            }
        }
        return results.toString().trim();
    }

    /**
     * Replaces all tool bindings for an agent with the provided list.
     */
    public List<AgentToolBinding> updateBindings(String agentId, List<String> toolTypeNames) {
        bindingRepository.deleteByAgentId(agentId);
        List<AgentToolBinding> newBindings = toolTypeNames.stream().map(name -> {
            AgentToolBinding b = new AgentToolBinding();
            b.setAgentId(agentId);
            b.setToolType(ToolType.valueOf(name));
            b.setEnabled(true);
            return b;
        }).collect(Collectors.toList());
        return bindingRepository.saveAll(newBindings);
    }

    public List<String> availableToolTypes() {
        return Arrays.stream(ToolType.values()).map(Enum::name).toList();
    }
}