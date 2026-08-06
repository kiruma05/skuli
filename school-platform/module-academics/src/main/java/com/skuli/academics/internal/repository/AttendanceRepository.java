package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Attendance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Attendance}.
 */
public interface AttendanceRepository
        extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {

    /** Tenant-scoped lookup — an attendance record is only visible to the school that owns it. */
    Optional<Attendance> findByIdAndTenantId(Integer id, String tenantId);
}
