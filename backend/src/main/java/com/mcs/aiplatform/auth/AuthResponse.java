package com.mcs.aiplatform.auth;

public record AuthResponse(
        String token,
        String userId,
        String email,
        String name
) {}
