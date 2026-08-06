package com.skuli.staff.internal.repository;

import com.skuli.staff.internal.domain.Teacher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Teacher}. Queries are tenant-scoped automatically via {@code @TenantId};
 * {@link JpaSpecificationExecutor} backs the {@code ?search=} list endpoint.
 */
public interface TeacherRepository
        extends JpaRepository<Teacher, String>, JpaSpecificationExecutor<Teacher> {

    /** Tenant-safe load-by-id (queries are tenant-filtered by {@code @TenantId}; find-by-PK is not). */
    @Override
    @Query("select t from Teacher t where t.id = ?1")
    Optional<Teacher> findById(String id);

    Optional<Teacher> findByUsername(String username);

    boolean existsByUsername(String username);
}
