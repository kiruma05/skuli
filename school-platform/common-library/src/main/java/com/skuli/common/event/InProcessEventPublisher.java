package com.skuli.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Default {@link EventPublisher} backed by Spring's in-process event bus. Events are delivered
 * synchronously within the same JVM. Replaced by a broker-backed publisher when cross-service
 * async or replay/audit needs arrive — without changing any call site.
 */
@Component
public class InProcessEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher delegate;

    public InProcessEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(DomainEvent event) {
        delegate.publishEvent(event);
    }
}
