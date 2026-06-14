package com.suplab.aether.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyRequest(
        @NotBlank(message = "yamlContent is required")
        String yamlContent,

        @NotBlank(message = "changedBy is required")
        String changedBy
) {}
