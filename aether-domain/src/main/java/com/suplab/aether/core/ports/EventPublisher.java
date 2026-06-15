package com.suplab.aether.core.ports;

import com.suplab.aether.core.events.DomainEvent;

import java.util.List;

public interface EventPublisher {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
