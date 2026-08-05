package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.SchoolClass;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link SchoolClass}.
 */
public interface SchoolClassRepository
        extends JpaRepository<SchoolClass, Integer>, JpaSpecificationExecutor<SchoolClass> {

    Optional<SchoolClass> findByName(String name);

    /** Tenant-scoped lookup — a class is only visible to the school that owns it. */
    Optional<SchoolClass> findByIdAndTenantId(Integer id, String tenantId);

    /** Backs the per-tenant name-uniqueness rule enforced by the service. */
    boolean existsByTenantIdAndName(String tenantId, String name);
}
