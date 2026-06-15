package com.suplab.aether.core.ports;

import com.suplab.aether.core.domain.AgentFeedback;
import com.suplab.aether.core.domain.TenantId;

import java.util.List;
import java.util.Map;

public interface AgentFeedbackPort {

    void record(AgentFeedback feedback);

    List<AgentFeedback> findByAgentType(TenantId tenantId, String agentType, int limit);

    /**
     * Returns per-agent accuracy stats: agentType -> {total, correct, accuracy}.
     */
    Map<String, Map<String, Object>> getPerformanceStats(TenantId tenantId);
}
