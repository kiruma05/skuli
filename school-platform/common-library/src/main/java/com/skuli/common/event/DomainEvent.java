package com.skuli.common.event;

import java.time.Instant;

/**
 * Marker for domain events published across module boundaries. Publishing goes through
 * {@link EventPublisher}; today's implementation is in-process, but call sites do not change
 * when a message broker (e.g. Kafka) is introduced later.
 */
public interface DomainEvent {

    /** When the event occurred. */
    Instant occurredAt();
}
