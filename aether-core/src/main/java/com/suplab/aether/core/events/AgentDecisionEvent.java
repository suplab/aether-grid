package com.suplab.aether.core.events;

import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record AgentDecisionEvent(
        UUID eventId,
        Instant occurredAt,
        ApiCallId callId,
        TenantId tenantId,
        String agentType,
        String decision,
        double confidence,
        boolean autoEnforced,
        String rationale
) implements DomainEvent {

    public AgentDecisionEvent(ApiCallId callId, TenantId tenantId, String agentType,
                               String decision, double confidence, boolean autoEnforced, String rationale) {
        this(UUID.randomUUID(), Instant.now(), callId, tenantId, agentType,
                decision, confidence, autoEnforced, rationale);
    }

    @Override
    public String eventType() {
        return "agent.decision";
    }
}
