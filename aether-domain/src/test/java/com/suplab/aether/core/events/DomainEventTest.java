package com.suplab.aether.core.events;

import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.HttpMethod;
import com.suplab.aether.core.domain.TenantId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void apiCallRecordedEvent_hasCorrectEventType() {
        var event = new ApiCallRecordedEvent(
                ApiCallId.generate(), TenantId.generate(),
                UUID.randomUUID(), HttpMethod.POST, "/v1/data"
        );

        assertThat(event.eventType()).isEqualTo("api.call.recorded");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void sealedHierarchy_exhaustivePatternMatching() {
        DomainEvent event = new ApiCallRecordedEvent(
                ApiCallId.generate(), TenantId.generate(),
                UUID.randomUUID(), HttpMethod.GET, "/v1/items"
        );

        String type = switch (event) {
            case ApiCallRecordedEvent e -> "recorded";
            case PolicyViolatedEvent e -> "violated";
            case AgentDecisionEvent e -> "decision";
            case GovernanceUpdatedEvent e -> "governance";
        };

        assertThat(type).isEqualTo("recorded");
    }

    @Test
    void policyViolatedEvent_hasCorrectEventType() {
        var event = new PolicyViolatedEvent(
                ApiCallId.generate(), TenantId.generate(),
                UUID.randomUUID(), "rate-limit-policy", "Request rate exceeded"
        );

        assertThat(event.eventType()).isEqualTo("policy.violated");
        assertThat(event.policyName()).isEqualTo("rate-limit-policy");
    }

    @Test
    void agentDecisionEvent_capturesConfidenceAndEnforcement() {
        var event = new AgentDecisionEvent(
                ApiCallId.generate(), TenantId.generate(),
                "GovernanceAgent", "ALERT", 0.95, true, "Anomalous parameter detected"
        );

        assertThat(event.eventType()).isEqualTo("agent.decision");
        assertThat(event.confidence()).isEqualTo(0.95);
        assertThat(event.autoEnforced()).isTrue();
    }
}
