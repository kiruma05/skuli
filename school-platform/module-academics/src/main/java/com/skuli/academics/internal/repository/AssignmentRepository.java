package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Assignment}.
 */
public interface AssignmentRepository
        extends JpaRepository<Assignment, Integer>, JpaSpecificationExecutor<Assignment> {
}
