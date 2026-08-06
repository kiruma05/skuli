package com.skuli.communication.internal.repository;

import com.skuli.communication.internal.domain.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Data access for {@link Event}. */
public interface EventRepository
        extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    /** Tenant-scoped lookup — an event is only visible to the school that owns it. */
    Optional<Event> findByIdAndTenantId(Integer id, String tenantId);
}
