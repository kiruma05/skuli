package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Assignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Assignment}.
 */
public interface AssignmentRepository
        extends JpaRepository<Assignment, Integer>, JpaSpecificationExecutor<Assignment> {

    /** Tenant-scoped lookup — an assignment is only visible to the school that owns it. */
    Optional<Assignment> findByIdAndTenantId(Integer id, String tenantId);
}
