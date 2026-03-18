package com.mcs.aiplatform.template;

import jakarta.validation.constraints.NotBlank;

public record CreateTemplateRequest(
        @NotBlank String name,
        String description,
        @NotBlank String content
) {}
