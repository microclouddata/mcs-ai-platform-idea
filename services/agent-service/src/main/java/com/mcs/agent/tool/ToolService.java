package com.mcs.agent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {

    private final AgentToolBindingRepository bindingRepository;

    @Cacheable(value = "agent-tools", key = "#agentId")
    public List<AgentToolBinding> getEnabledBindings(String agentId) {
        return bindingRepository.findByAgentIdAndEnabled(agentId, true);
    }

    public List<AgentToolBinding> getAllBindings(String agentId) {
        return bindingRepository.findByAgentId(agentId);
    }

    @CacheEvict(value = "agent-tools", key = "#agentId")
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
