package com.skuli.student.internal.repository;

import com.skuli.student.internal.domain.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Student}. Queries are tenant-scoped automatically via {@code @TenantId}.
 * {@link #countByClassId} backs the class-capacity check (reject a create when the class is full).
 */
public interface StudentRepository
        extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {

    /** Tenant-safe load-by-id (queries are tenant-filtered by {@code @TenantId}; find-by-PK is not). */
    @Override
    @Query("select s from Student s where s.id = ?1")
    Optional<Student> findById(String id);

    Optional<Student> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByClassId(Integer classId);
}
