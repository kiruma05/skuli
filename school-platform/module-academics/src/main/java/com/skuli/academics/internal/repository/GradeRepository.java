package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Grade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Grade}.
 */
public interface GradeRepository
        extends JpaRepository<Grade, Integer>, JpaSpecificationExecutor<Grade> {

    Optional<Grade> findByLevel(Integer level);

    /** Tenant-scoped lookup — a grade is only visible to the school that owns it. */
    Optional<Grade> findByIdAndTenantId(Integer id, String tenantId);

    /** Backs the per-tenant level-uniqueness rule enforced by the service. */
    boolean existsByTenantIdAndLevel(String tenantId, Integer level);
}
