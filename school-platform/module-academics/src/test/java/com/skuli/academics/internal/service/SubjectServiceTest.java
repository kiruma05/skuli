package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.SubjectDto;
import com.skuli.academics.internal.domain.Subject;
import com.skuli.academics.internal.mapper.SubjectMapperImpl;
import com.skuli.academics.internal.repository.SubjectRepository;
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
 * Unit tests for the reference vertical slice. Uses the real generated MapStruct mapper and a
 * mocked repository, with the tenant supplied via {@link TenantContext} the way the request
 * filter does in production.
 */
@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    private static final String TENANT = "default";

    @Mock
    private SubjectRepository repository;

    private SubjectService service;

    @BeforeEach
    void setUp() {
        service = new SubjectService(repository, new SubjectMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_persistsSubject_andIgnoresClientSuppliedId() {
        // Record the id observed at save-time; the same instance is later assigned a generated id,
        // so we cannot inspect it via a captor after the fact.
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        AtomicReference<String> nameAtSave = new AtomicReference<>();
        when(repository.existsByName("Mathematics")).thenReturn(false);
        when(repository.save(any(Subject.class))).thenAnswer(inv -> {
            Subject s = inv.getArgument(0);
            idAtSave.set(s.getId());
            nameAtSave.set(s.getName());
            s.setId(42);
            return s;
        });

        SubjectDto result = service.create(new SubjectDto(999, "Mathematics"));

        assertThat(idAtSave.get()).isNull(); // client id 999 discarded before save
        assertThat(nameAtSave.get()).isEqualTo("Mathematics");
        assertThat(result.id()).isEqualTo(42);
        assertThat(result.name()).isEqualTo("Mathematics");
    }

    @Test
    void create_rejectsDuplicateNameWithinTenant() {
        when(repository.existsByName("Mathematics")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new SubjectDto(null, "Mathematics")))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void get_missingSubjectInTenant_throwsNotFound() {
        when(repository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesTheTenantScopedSubject() {
        Subject subject = new Subject();
        subject.setId(5);
        subject.setName("History");
        when(repository.findById(5)).thenReturn(Optional.of(subject));

        service.delete(5);

        verify(repository).delete(subject);
    }
}
