package com.suplab.aether.api.controller;

import com.suplab.aether.api.dto.MemorySearchRequest;
import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.EmbeddingPort;
import com.suplab.aether.core.ports.MemoryStore;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/memory")
public class MemoryController {

    private final MemoryStore memoryStore;
    private final EmbeddingPort embeddingPort;

    public MemoryController(MemoryStore memoryStore, EmbeddingPort embeddingPort) {
        this.memoryStore = memoryStore;
        this.embeddingPort = embeddingPort;
    }

    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @PathVariable UUID tenantId,
            @RequestBody @Valid MemorySearchRequest request) {
        var tid = TenantId.of(tenantId.toString());
        var embedding = embeddingPort.embed(request.query());
        var records = memoryStore.findSimilar(tid, embedding, request.topK());
        return ResponseEntity.ok(records.stream().map(this::toMap).toList());
    }

    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID tenantId,
            @PathVariable UUID memoryId) {
        memoryStore.delete(TenantId.of(tenantId.toString()), memoryId);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(MemoryRecord record) {
        return Map.of(
                "id", record.id().toString(),
                "memoryType", record.memoryType().name(),
                "content", record.content(),
                "strength", record.strength(),
                "createdAt", record.createdAt().toString(),
                "lastAccessedAt", record.lastAccessedAt().toString()
        );
    }
}
