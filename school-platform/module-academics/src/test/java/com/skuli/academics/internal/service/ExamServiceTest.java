package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.ExamDto;
import com.skuli.academics.internal.domain.Exam;
import com.skuli.academics.internal.mapper.ExamMapperImpl;
import com.skuli.academics.internal.repository.ExamRepository;
import com.skuli.academics.internal.repository.LessonRepository;
import com.skuli.academics.internal.repository.SchoolClassRepository;
import com.skuli.academics.internal.repository.SubjectRepository;
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
class ExamServiceTest {

    private static final String TENANT = "default";

    @Mock
    private ExamRepository repository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SchoolClassRepository classRepository;

    private ExamService service;

    @BeforeEach
    void setUp() {
        service = new ExamService(repository, new ExamMapperImpl(),
                lessonRepository, subjectRepository, classRepository);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static ExamDto dto() {
        return new ExamDto(999, "Midterm", Instant.parse("2026-01-01T09:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"), 1, null, null, null, null);
    }

    @Test
    void create_persists_andIgnoresClientId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.save(any(Exam.class))).thenAnswer(inv -> {
            Exam e = inv.getArgument(0);
            idAtSave.set(e.getId());
            e.setId(3);
            return e;
        });

        ExamDto result = service.create(dto());

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(3);
        assertThat(result.title()).isEqualTo("Midterm");
    }

    @Test
    void get_missing_throwsNotFound() {
        when(repository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7)).isInstanceOf(ResourceNotFoundException.class);
    }
}
