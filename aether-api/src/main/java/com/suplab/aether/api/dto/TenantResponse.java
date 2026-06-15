package com.suplab.aether.api.dto;

import com.suplab.aether.core.domain.Tenant;

import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String status
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.id().value(),
                tenant.name(),
                tenant.status().name()
        );
    }
}
