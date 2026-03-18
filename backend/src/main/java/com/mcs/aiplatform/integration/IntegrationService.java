package com.mcs.aiplatform.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final RestClient.Builder restClientBuilder;

    public Integration create(String userId, CreateIntegrationRequest req) {
        Integration integration = new Integration();
        integration.setUserId(userId);
        integration.setType(req.type());
        integration.setName(req.name());
        integration.setConfig(req.config());
        return integrationRepository.save(integration);
    }

    public List<Integration> listForUser(String userId) {
        return integrationRepository.findByUserId(userId);
    }

    public void delete(String integrationId, String userId) {
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("Integration not found"));
        if (!integration.getUserId().equals(userId)) throw new RuntimeException("Access denied");
        integrationRepository.deleteById(integrationId);
    }

    public void toggleEnabled(String integrationId, String userId) {
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("Integration not found"));
        if (!integration.getUserId().equals(userId)) throw new RuntimeException("Access denied");
        integration.setEnabled(!integration.isEnabled());
        integrationRepository.save(integration);
    }

    public void triggerWebhook(String userId, String event, Map<String, Object> payload) {
        integrationRepository.findByUserIdAndEnabledTrue(userId).stream()
                .filter(i -> i.getType() == IntegrationType.WEBHOOK || i.getType() == IntegrationType.SLACK)
                .forEach(integration -> {
                    try {
                        String webhookUrl = integration.getConfig().get("webhookUrl");
                        if (webhookUrl == null || webhookUrl.isBlank()) return;
                        Map<String, Object> body = Map.of("event", event, "data", payload);
                        restClientBuilder.build()
                                .post()
                                .uri(webhookUrl)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(body)
                                .retrieve()
                                .toBodilessEntity();
                        integration.setLastTriggeredAt(Instant.now());
                        integrationRepository.save(integration);
                    } catch (Exception ignored) {}
                });
    }
}
