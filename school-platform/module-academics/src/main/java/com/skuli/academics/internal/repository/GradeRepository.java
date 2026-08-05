package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Grade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Grade}.
 */
public interface GradeRepository
        extends JpaRepository<Grade, Integer>, JpaSpecificationExecutor<Grade> {

    Optional<Grade> findByLevel(Integer level);
}
