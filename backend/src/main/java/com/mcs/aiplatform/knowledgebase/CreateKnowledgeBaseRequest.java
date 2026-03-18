package com.mcs.aiplatform.knowledgebase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateKnowledgeBaseRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description
) {}