package com.suplab.aether.policy.engine;

import java.time.Instant;
import java.util.List;

public record PolicyEvaluationResult(
        PolicyAction overallAction,
        List<RuleMatch> matches,
        Instant evaluatedAt
) {
    public PolicyEvaluationResult {
        if (overallAction == null) throw new IllegalArgumentException("overallAction must not be null");
        if (matches == null) matches = List.of();
        if (evaluatedAt == null) evaluatedAt = Instant.now();
    }

    public static PolicyEvaluationResult allow() {
        return new PolicyEvaluationResult(PolicyAction.ALLOW, List.of(), Instant.now());
    }

    public boolean isBlocked() {
        return overallAction == PolicyAction.BLOCK;
    }

    public boolean hasAlerts() {
        return matches.stream().anyMatch(m -> m.action() == PolicyAction.ALERT);
    }

    public record RuleMatch(String ruleName, PolicyAction action, String description) {}
}
