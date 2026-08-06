package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.SchoolClass;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link SchoolClass}. Queries are tenant-scoped automatically via
 * {@code @TenantId}.
 */
public interface SchoolClassRepository
        extends JpaRepository<SchoolClass, Integer>, JpaSpecificationExecutor<SchoolClass> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select c from SchoolClass c where c.id = ?1")
    Optional<SchoolClass> findById(Integer id);

    Optional<SchoolClass> findByName(String name);

    /** True when a class with this name exists in the current tenant. */
    boolean existsByName(String name);
}
