package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Exam;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Exam}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface ExamRepository
        extends JpaRepository<Exam, Integer>, JpaSpecificationExecutor<Exam> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select e from Exam e where e.id = ?1")
    Optional<Exam> findById(Integer id);
}
