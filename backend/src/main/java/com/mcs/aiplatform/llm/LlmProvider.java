package com.mcs.aiplatform.llm;

public interface LlmProvider {
    LlmResponse chat(LlmRequest request);
}
