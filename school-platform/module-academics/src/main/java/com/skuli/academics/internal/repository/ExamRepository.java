package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Exam}.
 */
public interface ExamRepository
        extends JpaRepository<Exam, Integer>, JpaSpecificationExecutor<Exam> {
}
