package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Assignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Assignment}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface AssignmentRepository
        extends JpaRepository<Assignment, Integer>, JpaSpecificationExecutor<Assignment> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select a from Assignment a where a.id = ?1")
    Optional<Assignment> findById(Integer id);
}
