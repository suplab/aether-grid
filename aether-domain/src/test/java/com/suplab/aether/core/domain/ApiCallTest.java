package com.suplab.aether.core.domain;

import com.suplab.aether.core.events.ApiCallRecordedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiCallTest {

    private static final TenantId TENANT_ID = TenantId.generate();
    private static final ApiEndpoint ENDPOINT = new ApiEndpoint(
            UUID.randomUUID(), "Test API", "https://api.example.com", "/v1/**"
    );

    @Test
    void record_raisesApiCallRecordedEvent() {
        var call = ApiCall.record(TENANT_ID, ENDPOINT, HttpMethod.POST, "/v1/users", "hash123");

        assertThat(call.id()).isNotNull();
        assertThat(call.tenantId()).isEqualTo(TENANT_ID);
        assertThat(call.outcome()).isEqualTo(CallOutcome.UNKNOWN);

        var events = call.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(ApiCallRecordedEvent.class);
        var event = (ApiCallRecordedEvent) events.getFirst();
        assertThat(event.callId()).isEqualTo(call.id());
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.method()).isEqualTo(HttpMethod.POST);
        assertThat(event.eventType()).isEqualTo("api.call.recorded");
    }

    @Test
    void pullDomainEvents_clearsQueueAfterPull() {
        var call = ApiCall.record(TENANT_ID, ENDPOINT, HttpMethod.GET, "/v1/items", null);

        assertThat(call.pullDomainEvents()).hasSize(1);
        assertThat(call.pullDomainEvents()).isEmpty();
    }

    @Test
    void complete_setsMetricsAndOutcome() {
        var call = ApiCall.record(TENANT_ID, ENDPOINT, HttpMethod.GET, "/v1/items", null);
        var metrics = new CallMetrics(200, 45L, Instant.now());

        call.complete(metrics, CallOutcome.SUCCESS);

        assertThat(call.outcome()).isEqualTo(CallOutcome.SUCCESS);
        assertThat(call.metrics()).isEqualTo(metrics);
        assertThat(call.metrics().isSuccess()).isTrue();
    }

    @Test
    void record_rejectsNullTenantId() {
        assertThatThrownBy(() -> ApiCall.record(null, ENDPOINT, HttpMethod.GET, "/v1/items", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId");
    }

    @Test
    void record_rejectsBlankPath() {
        assertThatThrownBy(() -> ApiCall.record(TENANT_ID, ENDPOINT, HttpMethod.GET, "  ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path");
    }
}
