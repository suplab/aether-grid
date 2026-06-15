package com.suplab.aether.agents.spi;

import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.TenantId;

import java.util.List;
import java.util.Map;

public record AgentInput(
        ApiCallId callId,
        TenantId tenantId,
        AgentCapability capability,
        String serialisedApiCall,
        List<MemoryRecord> relevantMemories,
        Map<String, Object> context
) {
    public AgentInput {
        if (callId == null) throw new IllegalArgumentException("callId must not be null");
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        if (capability == null) throw new IllegalArgumentException("capability must not be null");
        if (serialisedApiCall == null || serialisedApiCall.isBlank()) {
            throw new IllegalArgumentException("serialisedApiCall must not be blank");
        }
        if (relevantMemories == null) relevantMemories = List.of();
        if (context == null) context = Map.of();
    }
}
