package com.suplab.aether.core.domain;

import java.time.Instant;
import java.util.UUID;

public record AgentFeedback(
        UUID id,
        TenantId tenantId,
        String agentType,
        UUID decisionId,
        String originalDecision,
        double originalConfidence,
        DecisionOutcome outcome,
        String outcomeDetail,
        Instant recordedAt
) {
    public AgentFeedback {
        if (id == null) throw new IllegalArgumentException("AgentFeedback id must not be null");
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        if (agentType == null || agentType.isBlank()) throw new IllegalArgumentException("agentType must not be blank");
        if (decisionId == null) throw new IllegalArgumentException("decisionId must not be null");
        if (originalDecision == null || originalDecision.isBlank()) throw new IllegalArgumentException("originalDecision must not be blank");
        if (originalConfidence < 0.0 || originalConfidence > 1.0) {
            throw new IllegalArgumentException("originalConfidence must be between 0.0 and 1.0");
        }
        if (outcome == null) throw new IllegalArgumentException("outcome must not be null");
        if (recordedAt == null) throw new IllegalArgumentException("recordedAt must not be null");
    }

    public static AgentFeedback create(TenantId tenantId, String agentType, UUID decisionId,
                                       String originalDecision, double confidence,
                                       DecisionOutcome outcome, String outcomeDetail) {
        return new AgentFeedback(
                UUID.randomUUID(),
                tenantId,
                agentType,
                decisionId,
                originalDecision,
                confidence,
                outcome,
                outcomeDetail,
                Instant.now()
        );
    }
}
