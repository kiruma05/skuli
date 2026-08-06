package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Lesson;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Lesson}.
 */
public interface LessonRepository
        extends JpaRepository<Lesson, Integer>, JpaSpecificationExecutor<Lesson> {

    /** Tenant-scoped lookup — a lesson is only visible to the school that owns it. */
    Optional<Lesson> findByIdAndTenantId(Integer id, String tenantId);
}
