package com.skuli.student.internal.repository;

import com.skuli.student.internal.domain.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Student}. {@link #countByClassId} backs the Phase 3 class-capacity
 * check (reject a create when the class is full), preserved from the legacy server action.
 */
public interface StudentRepository
        extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByClassId(Integer classId);
}
