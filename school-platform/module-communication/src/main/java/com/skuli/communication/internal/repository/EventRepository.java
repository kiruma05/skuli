package com.skuli.communication.internal.repository;

import com.skuli.communication.internal.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Data access for {@link Event}. */
public interface EventRepository
        extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {
}
