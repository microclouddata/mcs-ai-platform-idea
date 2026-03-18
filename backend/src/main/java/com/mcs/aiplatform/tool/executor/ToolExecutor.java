package com.mcs.aiplatform.tool.executor;

import com.mcs.aiplatform.tool.AgentToolBinding;
import com.mcs.aiplatform.tool.ToolType;

public interface ToolExecutor {
    ToolType supportedType();
    String execute(String input, AgentToolBinding binding, String agentId);
}