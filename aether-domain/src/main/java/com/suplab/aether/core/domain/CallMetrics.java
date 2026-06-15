package com.suplab.aether.core.domain;

import java.time.Instant;

public record CallMetrics(
        int responseCode,
        long latencyMs,
        Instant capturedAt
) {
    public CallMetrics {
        if (responseCode < 100 || responseCode > 599) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + responseCode);
        }
        if (latencyMs < 0) {
            throw new IllegalArgumentException("Latency must not be negative");
        }
        if (capturedAt == null) {
            throw new IllegalArgumentException("capturedAt must not be null");
        }
    }

    public boolean isSuccess() {
        return responseCode >= 200 && responseCode < 300;
    }

    public boolean isClientError() {
        return responseCode >= 400 && responseCode < 500;
    }

    public boolean isServerError() {
        return responseCode >= 500;
    }
}
