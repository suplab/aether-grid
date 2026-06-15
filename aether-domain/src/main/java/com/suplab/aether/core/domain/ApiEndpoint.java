package com.suplab.aether.core.domain;

import java.util.UUID;

public record ApiEndpoint(
        UUID id,
        String name,
        String baseUrl,
        String pathPattern
) {
    public ApiEndpoint {
        if (id == null) throw new IllegalArgumentException("ApiEndpoint id must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("ApiEndpoint name must not be blank");
        if (baseUrl == null || baseUrl.isBlank()) throw new IllegalArgumentException("ApiEndpoint baseUrl must not be blank");
        if (pathPattern == null || pathPattern.isBlank()) throw new IllegalArgumentException("ApiEndpoint pathPattern must not be blank");
    }
}
