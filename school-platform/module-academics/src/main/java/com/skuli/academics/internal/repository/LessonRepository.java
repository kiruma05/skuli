package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Lesson;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Lesson}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface LessonRepository
        extends JpaRepository<Lesson, Integer>, JpaSpecificationExecutor<Lesson> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select l from Lesson l where l.id = ?1")
    Optional<Lesson> findById(Integer id);
}
