package com.skuli.communication.internal.repository;

import com.skuli.communication.internal.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Data access for {@link Announcement}. */
public interface AnnouncementRepository
        extends JpaRepository<Announcement, Integer>, JpaSpecificationExecutor<Announcement> {
}
