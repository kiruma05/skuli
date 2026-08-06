package com.skuli.communication.internal.repository;

import com.skuli.communication.internal.domain.Announcement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Announcement}. Queries are tenant-scoped automatically via
 * {@code @TenantId}.
 */
public interface AnnouncementRepository
        extends JpaRepository<Announcement, Integer>, JpaSpecificationExecutor<Announcement> {

    /** Tenant-safe load-by-id (queries are tenant-filtered by {@code @TenantId}; find-by-PK is not). */
    @Override
    @Query("select a from Announcement a where a.id = ?1")
    Optional<Announcement> findById(Integer id);
}
