package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Result;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Result}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface ResultRepository
        extends JpaRepository<Result, Integer>, JpaSpecificationExecutor<Result> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select r from Result r where r.id = ?1")
    Optional<Result> findById(Integer id);
}
