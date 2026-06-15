package com.suplab.aether.policy.engine;

public record PolicyRule(
        String name,
        String description,
        String condition,
        PolicyAction action,
        int priority
) {
    public PolicyRule {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("PolicyRule name must not be blank");
        if (condition == null || condition.isBlank()) throw new IllegalArgumentException("PolicyRule condition must not be blank");
        if (action == null) throw new IllegalArgumentException("PolicyRule action must not be null");
    }
}
