package com.mcs.aiplatform.apikey;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyResponse create(String userId, CreateApiKeyRequest req) {
        String rawKey = "mcs_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String hash = sha256(rawKey);
        String prefix = rawKey.substring(0, 12);

        ApiKey key = new ApiKey();
        key.setUserId(userId);
        key.setName(req.name());
        key.setKeyHash(hash);
        key.setKeyPrefix(prefix);
        key.setScopes(req.scopes() != null ? req.scopes() : List.of("*"));
        key.setExpiresAt(req.expiresAt());
        ApiKey saved = apiKeyRepository.save(key);

        return toResponse(saved, rawKey);
    }

    public List<ApiKeyResponse> listForUser(String userId) {
        return apiKeyRepository.findByUserId(userId).stream()
                .map(k -> toResponse(k, null))
                .toList();
    }

    public void revoke(String keyId, String userId) {
        apiKeyRepository.deleteByIdAndUserId(keyId, userId);
    }

    public Optional<ApiKey> authenticate(String rawKey) {
        String hash = sha256(rawKey);
        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHashAndEnabledTrue(hash);
        keyOpt.ifPresent(key -> {
            if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(Instant.now())) {
                return;
            }
            key.setLastUsedAt(Instant.now());
            apiKeyRepository.save(key);
        });
        return keyOpt.filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(Instant.now()));
    }

    private ApiKeyResponse toResponse(ApiKey k, String plainKey) {
        return new ApiKeyResponse(k.getId(), k.getName(), k.getKeyPrefix(), plainKey,
                k.getScopes(), k.isEnabled(), k.getLastUsedAt(), k.getExpiresAt(), k.getCreatedAt());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
