package com.suplab.aether.proxy.filter;

import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;

public record TenantContext(Tenant tenant) {

    public TenantId tenantId() {
        return tenant.id();
    }

    public static final String EXCHANGE_ATTR = "AETHER_TENANT_CONTEXT";
}
