package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Subject}. Subject is the Phase 3 reference vertical slice.
 */
public interface SubjectRepository
        extends JpaRepository<Subject, Integer>, JpaSpecificationExecutor<Subject> {

    Optional<Subject> findByName(String name);
}
