package com.skuli.student.internal.repository;

import com.skuli.student.internal.domain.Parent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Parent}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface ParentRepository
        extends JpaRepository<Parent, String>, JpaSpecificationExecutor<Parent> {

    /** Tenant-safe load-by-id (queries are tenant-filtered by {@code @TenantId}; find-by-PK is not). */
    @Override
    @Query("select p from Parent p where p.id = ?1")
    Optional<Parent> findById(String id);

    Optional<Parent> findByUsername(String username);

    boolean existsByUsername(String username);
}
