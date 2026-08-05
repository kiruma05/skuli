package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.SchoolClassDto;
import com.skuli.academics.internal.domain.SchoolClass;
import com.skuli.academics.internal.mapper.SchoolClassMapperImpl;
import com.skuli.academics.internal.repository.SchoolClassRepository;
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
 * Unit tests for the class-group slice, following the Subject reference pattern.
 */
@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    private static final String TENANT = "default";

    @Mock
    private SchoolClassRepository repository;

    private SchoolClassService service;

    @BeforeEach
    void setUp() {
        service = new SchoolClassService(repository, new SchoolClassMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_persistsClass_andIgnoresClientSuppliedId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.existsByTenantIdAndName(TENANT, "1A")).thenReturn(false);
        when(repository.save(any(SchoolClass.class))).thenAnswer(inv -> {
            SchoolClass c = inv.getArgument(0);
            idAtSave.set(c.getId());
            c.setId(11);
            return c;
        });

        SchoolClassDto result = service.create(new SchoolClassDto(999, "1A", 30, "teacher-1", 1));

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(11);
        assertThat(result.name()).isEqualTo("1A");
        assertThat(result.capacity()).isEqualTo(30);
        assertThat(result.gradeId()).isEqualTo(1);
    }

    @Test
    void create_rejectsDuplicateNameWithinTenant() {
        when(repository.existsByTenantIdAndName(TENANT, "1A")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new SchoolClassDto(null, "1A", 30, null, 1)))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_changesMutableFields() {
        SchoolClass existing = new SchoolClass();
        existing.setId(5);
        existing.setName("1A");
        existing.setCapacity(30);
        existing.setGradeId(1);
        when(repository.findByIdAndTenantId(5, TENANT)).thenReturn(Optional.of(existing));
        when(repository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

        SchoolClassDto result = service.update(5, new SchoolClassDto(5, "1A", 40, "teacher-2", 2));

        assertThat(result.capacity()).isEqualTo(40);
        assertThat(result.supervisorId()).isEqualTo("teacher-2");
        assertThat(result.gradeId()).isEqualTo(2);
    }

    @Test
    void get_missingClassInTenant_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
