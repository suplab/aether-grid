package com.suplab.aether.memory.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.MemoryType;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.EmbeddingPort;
import com.suplab.aether.core.ports.MemoryStore;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Map;
import java.util.UUID;

public class ApiCallMemoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApiCallMemoryConsumer.class);

    private final EmbeddingPort embeddingPort;
    private final MemoryStore memoryStore;
    private final ObjectMapper objectMapper;

    public ApiCallMemoryConsumer(EmbeddingPort embeddingPort, MemoryStore memoryStore) {
        this.embeddingPort = embeddingPort;
        this.memoryStore = memoryStore;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "${aether.kafka.topics.api-calls:aether.api.calls}",
                   groupId = "${spring.kafka.consumer.group-id:aether-memory}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(record.value(), Map.class);
            processApiCallEvent(payload);
        } catch (Exception e) {
            log.error("Failed to process api.call event at offset={} partition={}: {}",
                    record.offset(), record.partition(), e.getMessage());
        }
    }

    private void processApiCallEvent(Map<String, Object> payload) {
        var tenantId = TenantId.of((String) payload.get("tenantId"));
        var callId = UUID.fromString((String) payload.get("callId"));
        var method = (String) payload.get("method");
        var path = (String) payload.get("path");
        var responseCode = payload.get("responseCode") != null
                ? ((Number) payload.get("responseCode")).intValue() : 0;
        var latencyMs = payload.get("latencyMs") != null
                ? ((Number) payload.get("latencyMs")).longValue() : 0L;
        var outcome = (String) payload.getOrDefault("outcome", "UNKNOWN");

        var textToEmbed = buildEmbeddingText(method, path, responseCode, latencyMs, outcome);
        float[] embedding;
        try {
            embedding = embeddingPort.embed(textToEmbed);
        } catch (Exception e) {
            log.warn("Embedding failed for call {} — skipping memory storage: {}", callId, e.getMessage());
            return;
        }

        var memoryRecord = MemoryRecord.create(
                tenantId,
                resolveMemoryType(responseCode, outcome),
                textToEmbed,
                embedding,
                callId
        );

        memoryStore.store(memoryRecord);
        log.debug("Stored memory for callId={} tenant={} type={}", callId, tenantId, memoryRecord.memoryType());
    }

    private String buildEmbeddingText(String method, String path, int responseCode, long latencyMs, String outcome) {
        return String.format("%s %s responded %d in %dms outcome=%s", method, path, responseCode, latencyMs, outcome);
    }

    private MemoryType resolveMemoryType(int responseCode, String outcome) {
        if ("FAILURE".equals(outcome) || responseCode >= 500) return MemoryType.EPISODIC;
        if ("TIMEOUT".equals(outcome)) return MemoryType.EPISODIC;
        if (responseCode >= 400) return MemoryType.SEMANTIC;
        return MemoryType.PROCEDURAL;
    }
}
