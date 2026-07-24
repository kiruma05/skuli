package com.skuli.common.event;

/**
 * Abstraction for publishing {@link DomainEvent}s. Cross-module communication goes through this
 * interface (or an exposed service API), never a direct repository call. Swapping the in-process
 * implementation for a broker-backed one later leaves call sites untouched.
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}
