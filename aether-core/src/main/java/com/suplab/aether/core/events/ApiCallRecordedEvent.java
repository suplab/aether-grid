package com.suplab.aether.core.events;

import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.HttpMethod;
import com.suplab.aether.core.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ApiCallRecordedEvent(
        UUID eventId,
        Instant occurredAt,
        ApiCallId callId,
        TenantId tenantId,
        UUID endpointId,
        HttpMethod method,
        String path
) implements DomainEvent {

    public ApiCallRecordedEvent(ApiCallId callId, TenantId tenantId,
                                 UUID endpointId, HttpMethod method, String path) {
        this(UUID.randomUUID(), Instant.now(), callId, tenantId, endpointId, method, path);
    }

    @Override
    public String eventType() {
        return "api.call.recorded";
    }
}
