package com.skuli.communication.internal.repository;

import com.skuli.communication.internal.domain.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/** Data access for {@link Event}. Queries are tenant-scoped automatically via {@code @TenantId}. */
public interface EventRepository
        extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    /** Tenant-safe load-by-id (queries are tenant-filtered by {@code @TenantId}; find-by-PK is not). */
    @Override
    @Query("select e from Event e where e.id = ?1")
    Optional<Event> findById(Integer id);
}
