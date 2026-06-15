package com.suplab.aether.memory.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.MemoryType;
import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.domain.TenantStatus;
import com.suplab.aether.core.ports.EmbeddingPort;
import com.suplab.aether.core.ports.MemoryStore;
import com.suplab.aether.core.ports.TenantRepository;
import com.suplab.aether.policy.gdpr.GdprRedactionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiCallMemoryConsumerTest {

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private MemoryStore memoryStore;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private GdprRedactionService gdprRedactionService;

    private ApiCallMemoryConsumer consumer;

    private static final float[] FAKE_EMBEDDING = new float[384];
    private static final UUID TENANT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        consumer = new ApiCallMemoryConsumer(embeddingPort, memoryStore, tenantRepository, gdprRedactionService);
        // Default: tenant exists and has not opted out; redaction is a pass-through
        var activeTenant = Tenant.reconstitute(
                TenantId.of(TENANT_UUID.toString()), "Test Tenant", "hash", TenantStatus.ACTIVE, false);
        when(tenantRepository.findById(any())).thenReturn(Optional.of(activeTenant));
        when(gdprRedactionService.redact(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void successful_call_stored_as_PROCEDURAL_memory() throws Exception {
        when(embeddingPort.embed(anyString())).thenReturn(FAKE_EMBEDDING);
        var payload = buildPayload(TENANT_UUID, "GET", "/v1/data", 200, 45L, "SUCCESS");
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 0, UUID.randomUUID().toString(), payload));

        var captor = ArgumentCaptor.forClass(MemoryRecord.class);
        verify(memoryStore).store(captor.capture());
        assertThat(captor.getValue().memoryType()).isEqualTo(MemoryType.PROCEDURAL);
    }

    @Test
    void server_error_stored_as_EPISODIC_memory() throws Exception {
        when(embeddingPort.embed(anyString())).thenReturn(FAKE_EMBEDDING);
        var payload = buildPayload(TENANT_UUID, "POST", "/v1/items", 500, 200L, "FAILURE");
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 1, UUID.randomUUID().toString(), payload));

        var captor = ArgumentCaptor.forClass(MemoryRecord.class);
        verify(memoryStore).store(captor.capture());
        assertThat(captor.getValue().memoryType()).isEqualTo(MemoryType.EPISODIC);
    }

    @Test
    void client_error_stored_as_SEMANTIC_memory() throws Exception {
        when(embeddingPort.embed(anyString())).thenReturn(FAKE_EMBEDDING);
        var payload = buildPayload(TENANT_UUID, "GET", "/v1/missing", 404, 15L, "UNKNOWN");
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 2, UUID.randomUUID().toString(), payload));

        var captor = ArgumentCaptor.forClass(MemoryRecord.class);
        verify(memoryStore).store(captor.capture());
        assertThat(captor.getValue().memoryType()).isEqualTo(MemoryType.SEMANTIC);
    }

    @Test
    void embedding_failure_skips_storage_without_throwing() {
        when(embeddingPort.embed(anyString())).thenThrow(new RuntimeException("Ollama unavailable"));
        var payload = buildPayload(TENANT_UUID, "GET", "/v1/data", 200, 10L, "SUCCESS");

        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 3, UUID.randomUUID().toString(), payload));

        verify(memoryStore, never()).store(any());
    }

    @Test
    void malformed_payload_does_not_propagate_exception() {
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 4, "key", "not-json"));
        verify(memoryStore, never()).store(any());
    }

    @Test
    void optedOutTenant_skipsMemoryStorage() throws Exception {
        var optedOutTenant = Tenant.reconstitute(
                TenantId.of(TENANT_UUID.toString()), "Test Tenant", "hash", TenantStatus.ACTIVE, true);
        when(tenantRepository.findById(TenantId.of(TENANT_UUID.toString()))).thenReturn(Optional.of(optedOutTenant));

        var payload = buildPayload(TENANT_UUID, "GET", "/v1/data", 200, 10L, "SUCCESS");
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 5, UUID.randomUUID().toString(), payload));

        verify(memoryStore, never()).store(any());
        verify(embeddingPort, never()).embed(anyString());
    }

    @Test
    void unknownTenant_skipsMemoryStorage() throws Exception {
        when(tenantRepository.findById(any())).thenReturn(Optional.empty());

        var payload = buildPayload(UUID.randomUUID(), "GET", "/v1/data", 200, 10L, "SUCCESS");
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 6, UUID.randomUUID().toString(), payload));

        verify(memoryStore, never()).store(any());
        verify(embeddingPort, never()).embed(anyString());
    }

    @Test
    void contentIsRedactedBeforeStorage() throws Exception {
        when(embeddingPort.embed(anyString())).thenReturn(FAKE_EMBEDDING);
        when(gdprRedactionService.redact(anyString())).thenReturn("REDACTED_CONTENT");

        var payload = buildPayload(TENANT_UUID, "POST", "/v1/data", 200, 10L, "SUCCESS");
        consumer.consume(new ConsumerRecord<>("aether.api.calls", 0, 7, UUID.randomUUID().toString(), payload));

        var captor = ArgumentCaptor.forClass(MemoryRecord.class);
        verify(memoryStore).store(captor.capture());
        assertThat(captor.getValue().content()).isEqualTo("REDACTED_CONTENT");
        verify(embeddingPort).embed("REDACTED_CONTENT");
    }

    private String buildPayload(UUID tenantId, String method, String path,
                                int code, long latencyMs, String outcome) {
        try {
            return new ObjectMapper().writeValueAsString(java.util.Map.of(
                    "callId", UUID.randomUUID().toString(),
                    "tenantId", tenantId.toString(),
                    "method", method,
                    "path", path,
                    "responseCode", code,
                    "latencyMs", latencyMs,
                    "outcome", outcome
            ));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
