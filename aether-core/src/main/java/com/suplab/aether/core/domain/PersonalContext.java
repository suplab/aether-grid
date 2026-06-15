package com.suplab.aether.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PersonalContext(
        String userId,
        TenantId tenantId,
        List<String> recentMemorySummaries,
        Map<String, Object> preferences,
        String emotionalState,
        double engagementScore,
        Instant fetchedAt
) {
    public PersonalContext {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        recentMemorySummaries = recentMemorySummaries != null ? List.copyOf(recentMemorySummaries) : List.of();
        preferences = preferences != null ? Map.copyOf(preferences) : Map.of();
        if (emotionalState == null || emotionalState.isBlank()) emotionalState = "NEUTRAL";
        if (fetchedAt == null) fetchedAt = Instant.now();
    }

    public static PersonalContext empty(TenantId tenantId, String userId) {
        return new PersonalContext(userId, tenantId, List.of(), Map.of(), "NEUTRAL", 0.5, Instant.now());
    }
}
