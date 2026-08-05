package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Subject}. Subject is the Phase 3 reference vertical slice.
 */
public interface SubjectRepository
        extends JpaRepository<Subject, Integer>, JpaSpecificationExecutor<Subject> {

    Optional<Subject> findByName(String name);

    /** Tenant-scoped lookup — a subject is only visible to the school that owns it. */
    Optional<Subject> findByIdAndTenantId(Integer id, String tenantId);

    /** Backs the per-tenant name-uniqueness rule enforced by the service. */
    boolean existsByTenantIdAndName(String tenantId, String name);
}
