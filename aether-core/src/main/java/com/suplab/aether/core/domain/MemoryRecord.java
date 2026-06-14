package com.suplab.aether.core.domain;

import java.time.Instant;
import java.util.UUID;

public record MemoryRecord(
        UUID id,
        TenantId tenantId,
        MemoryType memoryType,
        String content,
        float[] embedding,
        double strength,
        UUID sourceCallId,
        Instant createdAt,
        Instant lastAccessedAt
) {
    public MemoryRecord {
        if (id == null) throw new IllegalArgumentException("MemoryRecord id must not be null");
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        if (memoryType == null) throw new IllegalArgumentException("memoryType must not be null");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content must not be blank");
        if (embedding == null || embedding.length != 384) {
            throw new IllegalArgumentException("embedding must be exactly 384 dimensions (all-MiniLM-L6-v2)");
        }
        if (strength < 0.0 || strength > 1.0) throw new IllegalArgumentException("strength must be between 0.0 and 1.0");
        if (createdAt == null) throw new IllegalArgumentException("createdAt must not be null");
        if (lastAccessedAt == null) throw new IllegalArgumentException("lastAccessedAt must not be null");
    }

    public static MemoryRecord create(TenantId tenantId, MemoryType memoryType,
                                      String content, float[] embedding, UUID sourceCallId) {
        var now = Instant.now();
        return new MemoryRecord(UUID.randomUUID(), tenantId, memoryType, content,
                embedding, 1.0, sourceCallId, now, now);
    }
}
