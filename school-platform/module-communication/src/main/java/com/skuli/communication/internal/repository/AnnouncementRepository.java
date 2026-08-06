package com.skuli.communication.internal.repository;

import com.skuli.communication.internal.domain.Announcement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Data access for {@link Announcement}. */
public interface AnnouncementRepository
        extends JpaRepository<Announcement, Integer>, JpaSpecificationExecutor<Announcement> {

    /** Tenant-scoped lookup — an announcement is only visible to the school that owns it. */
    Optional<Announcement> findByIdAndTenantId(Integer id, String tenantId);
}
