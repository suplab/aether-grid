package com.suplab.aether.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MemorySearchRequest(
        @NotBlank(message = "query is required")
        String query,

        @Min(1) @Max(50)
        int topK
) {
    public MemorySearchRequest {
        if (topK <= 0) topK = 10;
    }
}
