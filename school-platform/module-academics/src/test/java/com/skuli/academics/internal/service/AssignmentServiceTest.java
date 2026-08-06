package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.AssignmentDto;
import com.skuli.academics.internal.domain.Assignment;
import com.skuli.academics.internal.mapper.AssignmentMapperImpl;
import com.skuli.academics.internal.repository.AssignmentRepository;
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
class AssignmentServiceTest {

    private static final String TENANT = "default";

    @Mock
    private AssignmentRepository repository;

    private AssignmentService service;

    @BeforeEach
    void setUp() {
        service = new AssignmentService(repository, new AssignmentMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static AssignmentDto dto() {
        return new AssignmentDto(999, "Homework 1", Instant.parse("2026-01-01T09:00:00Z"),
                Instant.parse("2026-01-08T09:00:00Z"), 1);
    }

    @Test
    void create_persists_andIgnoresClientId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            idAtSave.set(a.getId());
            a.setId(4);
            return a;
        });

        AssignmentDto result = service.create(dto());

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(4);
        assertThat(result.title()).isEqualTo("Homework 1");
    }

    @Test
    void get_missing_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7)).isInstanceOf(ResourceNotFoundException.class);
    }
}
