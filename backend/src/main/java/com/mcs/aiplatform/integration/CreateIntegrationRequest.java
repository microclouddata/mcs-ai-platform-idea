package com.mcs.aiplatform.integration;
import java.util.Map;
public record CreateIntegrationRequest(IntegrationType type, String name, Map<String, String> config) {}
