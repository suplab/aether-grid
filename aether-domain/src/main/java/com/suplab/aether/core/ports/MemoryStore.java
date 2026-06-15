package com.suplab.aether.core.ports;

import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.MemoryType;
import com.suplab.aether.core.domain.TenantId;

import java.util.List;
import java.util.UUID;

public interface MemoryStore {

    void store(MemoryRecord record);

    List<MemoryRecord> findSimilar(TenantId tenantId, float[] embedding, int topK);

    List<MemoryRecord> findByType(TenantId tenantId, MemoryType type, int limit);

    void delete(TenantId tenantId, UUID memoryId);

    void deleteAll(TenantId tenantId);
}
