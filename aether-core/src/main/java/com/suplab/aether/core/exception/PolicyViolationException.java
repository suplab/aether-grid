package com.suplab.aether.core.exception;

import com.suplab.aether.core.domain.TenantId;

import java.util.UUID;

public final class PolicyViolationException extends AetherException {

    private final TenantId tenantId;
    private final UUID policyId;

    public PolicyViolationException(TenantId tenantId, UUID policyId, String detail) {
        super("Policy violation for tenant " + tenantId + " policy " + policyId + ": " + detail);
        this.tenantId = tenantId;
        this.policyId = policyId;
    }

    public TenantId tenantId() { return tenantId; }
    public UUID policyId() { return policyId; }
}
