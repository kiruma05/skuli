package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Grade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Grade}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface GradeRepository
        extends JpaRepository<Grade, Integer>, JpaSpecificationExecutor<Grade> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select g from Grade g where g.id = ?1")
    Optional<Grade> findById(Integer id);

    Optional<Grade> findByLevel(Integer level);

    /** True when a grade with this level exists in the current tenant. */
    boolean existsByLevel(Integer level);
}
