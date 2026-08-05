package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Attendance}.
 */
public interface AttendanceRepository
        extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {
}
