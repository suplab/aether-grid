package com.suplab.aether.core.events;

import com.suplab.aether.core.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record GovernanceUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UUID policyId,
        String updateType,
        String changedBy
) implements DomainEvent {

    public GovernanceUpdatedEvent(TenantId tenantId, UUID policyId, String updateType, String changedBy) {
        this(UUID.randomUUID(), Instant.now(), tenantId, policyId, updateType, changedBy);
    }

    @Override
    public String eventType() {
        return "governance.updated";
    }
}
