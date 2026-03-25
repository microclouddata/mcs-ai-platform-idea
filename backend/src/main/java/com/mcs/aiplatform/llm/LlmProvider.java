package com.mcs.aiplatform.llm;

import java.util.List;

public interface LlmProvider {
    LlmResponse chat(LlmRequest request);
    List<String> getAvailableModels();
}
