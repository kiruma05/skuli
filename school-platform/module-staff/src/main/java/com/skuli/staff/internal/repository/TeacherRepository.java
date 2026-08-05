package com.skuli.staff.internal.repository;

import com.skuli.staff.internal.domain.Teacher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Teacher}. {@link JpaSpecificationExecutor} is included now so the
 * Phase 3 list endpoints can build {@code ?search=} predicates without a repository change.
 */
public interface TeacherRepository
        extends JpaRepository<Teacher, String>, JpaSpecificationExecutor<Teacher> {

    Optional<Teacher> findByUsername(String username);

    boolean existsByUsername(String username);
}
