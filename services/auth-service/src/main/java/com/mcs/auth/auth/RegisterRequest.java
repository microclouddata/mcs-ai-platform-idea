package com.mcs.auth.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank String password) {}
