package com.suplab.aether.core.events;

import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record PolicyViolatedEvent(
        UUID eventId,
        Instant occurredAt,
        ApiCallId callId,
        TenantId tenantId,
        UUID policyId,
        String policyName,
        String violationDetail
) implements DomainEvent {

    public PolicyViolatedEvent(ApiCallId callId, TenantId tenantId,
                                UUID policyId, String policyName, String violationDetail) {
        this(UUID.randomUUID(), Instant.now(), callId, tenantId, policyId, policyName, violationDetail);
    }

    @Override
    public String eventType() {
        return "policy.violated";
    }
}
