package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Subject}. All queries are automatically scoped to the current tenant by
 * Hibernate's {@code @TenantId} discriminator, so no tenant argument is needed here.
 */
public interface SubjectRepository
        extends JpaRepository<Subject, Integer>, JpaSpecificationExecutor<Subject> {

    /**
     * Tenant-safe load-by-id. Overrides the inherited {@code findById} (which uses
     * {@code EntityManager.find} and is NOT tenant-filtered, because the primary key is globally
     * unique) with a JPQL query, to which {@code @TenantId} applies — so one tenant can never load
     * another's row by id.
     */
    @Override
    @Query("select s from Subject s where s.id = ?1")
    Optional<Subject> findById(Integer id);

    Optional<Subject> findByName(String name);

    /** True when a subject with this name exists in the current tenant. */
    boolean existsByName(String name);
}
