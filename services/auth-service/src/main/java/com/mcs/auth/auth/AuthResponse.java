package com.mcs.auth.auth;
public record AuthResponse(String token, String userId, String email, String name, String role) {}
