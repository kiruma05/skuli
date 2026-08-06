package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Attendance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link Attendance}. Queries are tenant-scoped automatically via {@code @TenantId}.
 */
public interface AttendanceRepository
        extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {

    /** Tenant-safe load-by-id (see {@link SubjectRepository#findById}). */
    @Override
    @Query("select a from Attendance a where a.id = ?1")
    Optional<Attendance> findById(Integer id);
}
