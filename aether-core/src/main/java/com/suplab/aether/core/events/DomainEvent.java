package com.suplab.aether.core.events;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent
        permits ApiCallRecordedEvent, PolicyViolatedEvent, AgentDecisionEvent, GovernanceUpdatedEvent {

    UUID eventId();
    Instant occurredAt();
    String eventType();
}
