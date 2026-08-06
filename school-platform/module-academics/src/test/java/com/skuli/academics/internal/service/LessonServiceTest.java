package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.LessonDto;
import com.skuli.academics.internal.domain.Lesson;
import com.skuli.academics.internal.mapper.LessonMapperImpl;
import com.skuli.academics.internal.repository.LessonRepository;
import com.skuli.common.domain.Day;
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
class LessonServiceTest {

    private static final String TENANT = "default";

    @Mock
    private LessonRepository repository;

    private LessonService service;

    @BeforeEach
    void setUp() {
        service = new LessonService(repository, new LessonMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static LessonDto dto() {
        return new LessonDto(999, "Algebra", Day.MONDAY, Instant.parse("2026-01-01T09:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"), 1, 2, "teacher-1");
    }

    @Test
    void create_persists_andIgnoresClientId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.save(any(Lesson.class))).thenAnswer(inv -> {
            Lesson l = inv.getArgument(0);
            idAtSave.set(l.getId());
            l.setId(5);
            return l;
        });

        LessonDto result = service.create(dto());

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(5);
        assertThat(result.name()).isEqualTo("Algebra");
    }

    @Test
    void get_missing_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7)).isInstanceOf(ResourceNotFoundException.class);
    }
}
