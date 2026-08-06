package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.AttendanceDto;
import com.skuli.academics.internal.domain.Attendance;
import com.skuli.academics.internal.mapper.AttendanceMapperImpl;
import com.skuli.academics.internal.repository.AttendanceRepository;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private static final String TENANT = "default";

    @Mock
    private AttendanceRepository repository;

    private AttendanceService service;

    @BeforeEach
    void setUp() {
        service = new AttendanceService(repository, new AttendanceMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static AttendanceDto dto() {
        return new AttendanceDto(999, Instant.parse("2026-01-01T09:00:00Z"), true, "student-1", 1);
    }

    @Test
    void create_persists_andIgnoresClientId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.save(any(Attendance.class))).thenAnswer(inv -> {
            Attendance a = inv.getArgument(0);
            idAtSave.set(a.getId());
            a.setId(6);
            return a;
        });

        AttendanceDto result = service.create(dto());

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(6);
        assertThat(result.present()).isTrue();
    }

    @Test
    void get_missing_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7)).isInstanceOf(ResourceNotFoundException.class);
    }
}
