package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Exam;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Exam}.
 */
public interface ExamRepository
        extends JpaRepository<Exam, Integer>, JpaSpecificationExecutor<Exam> {

    /** Tenant-scoped lookup — an exam is only visible to the school that owns it. */
    Optional<Exam> findByIdAndTenantId(Integer id, String tenantId);
}
