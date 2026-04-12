package com.mcs.chat.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmProviderFactory {

    private final Map<String, LlmProvider> providers;

    public LlmProvider get(String provider) {
        LlmProvider p = providers.get(provider != null ? provider.toUpperCase() : "OPENAI");
        if (p == null) throw new IllegalArgumentException("Unknown LLM provider: " + provider);
        return p;
    }
}
