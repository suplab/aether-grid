package com.suplab.aether.policy.engine;

import java.util.Map;

public record PolicyEvaluationContext(
        String method,
        String path,
        int responseCode,
        long latencyMs,
        String outcome,
        String tenantId,
        Map<String, Object> requestHeaders,
        Map<String, Object> metadata
) {
    public PolicyEvaluationContext {
        if (method == null || method.isBlank()) throw new IllegalArgumentException("method must not be blank");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        if (tenantId == null || tenantId.isBlank()) throw new IllegalArgumentException("tenantId must not be blank");
        if (requestHeaders == null) requestHeaders = Map.of();
        if (metadata == null) metadata = Map.of();
    }
}
