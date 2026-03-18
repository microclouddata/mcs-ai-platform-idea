package com.mcs.aiplatform.llm;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LlmProviderFactory {

    private final Map<String, LlmProvider> providers;

    public LlmProviderFactory(Map<String, LlmProvider> providers) {
        this.providers = providers;
    }

    public LlmProvider get(String provider) {
        LlmProvider llmProvider = providers.get(provider == null ? "OPENAI" : provider.toUpperCase());
        if (llmProvider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
        return llmProvider;
    }
}
