package com.suplab.aether.core.exception;

import com.suplab.aether.core.domain.TenantId;

public final class TenantNotFoundException extends AetherException {

    public TenantNotFoundException(TenantId tenantId) {
        super("Tenant not found: " + tenantId);
    }

    public TenantNotFoundException(String apiKeyHashPrefix) {
        super("Tenant not found for api key hash prefix: " + apiKeyHashPrefix);
    }
}
