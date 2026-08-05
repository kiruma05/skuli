package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.GradeDto;
import com.skuli.academics.internal.domain.Grade;
import com.skuli.academics.internal.mapper.GradeMapperImpl;
import com.skuli.academics.internal.repository.GradeRepository;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the grade slice, following the Subject reference pattern; uniqueness is on level.
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    private static final String TENANT = "default";

    @Mock
    private GradeRepository repository;

    private GradeService service;

    @BeforeEach
    void setUp() {
        service = new GradeService(repository, new GradeMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_persistsGrade_andIgnoresClientSuppliedId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.existsByTenantIdAndLevel(TENANT, 3)).thenReturn(false);
        when(repository.save(any(Grade.class))).thenAnswer(inv -> {
            Grade g = inv.getArgument(0);
            idAtSave.set(g.getId());
            g.setId(8);
            return g;
        });

        GradeDto result = service.create(new GradeDto(999, 3));

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(8);
        assertThat(result.level()).isEqualTo(3);
    }

    @Test
    void create_rejectsDuplicateLevelWithinTenant() {
        when(repository.existsByTenantIdAndLevel(TENANT, 3)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new GradeDto(null, 3)))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void get_missingGradeInTenant_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
