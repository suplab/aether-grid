package com.suplab.aether.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRequest(
        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be <= 200 characters")
        String name,

        @NotBlank(message = "apiKey is required")
        @Size(min = 32, message = "apiKey must be at least 32 characters")
        String apiKey
) {}
