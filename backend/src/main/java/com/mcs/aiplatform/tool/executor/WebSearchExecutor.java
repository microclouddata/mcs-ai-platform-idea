package com.mcs.aiplatform.tool.executor;

import com.mcs.aiplatform.tool.AgentToolBinding;
import com.mcs.aiplatform.tool.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSearchExecutor implements ToolExecutor {

    @Override
    public ToolType supportedType() {
        return ToolType.WEB_SEARCH;
    }

    @Override
    public String execute(String query, AgentToolBinding binding, String agentId) {
        // Stub — integrate a search API (e.g. Tavily, SerpAPI) via binding.getConfig()
        log.info("Web search requested: {}", query);
        return String.format("[Web search for '%s' — configure a search API key in tool settings to enable.]", query);
    }
}