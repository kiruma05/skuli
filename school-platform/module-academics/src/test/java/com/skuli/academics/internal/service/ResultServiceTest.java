package com.skuli.academics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.dto.ResultDto;
import com.skuli.academics.internal.domain.Result;
import com.skuli.academics.internal.mapper.ResultMapperImpl;
import com.skuli.academics.internal.repository.ResultRepository;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    private static final String TENANT = "default";

    @Mock
    private ResultRepository repository;

    private ResultService service;

    @BeforeEach
    void setUp() {
        service = new ResultService(repository, new ResultMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_withExamOnly_persists() {
        when(repository.save(any(Result.class))).thenAnswer(inv -> {
            Result r = inv.getArgument(0);
            r.setId(1);
            return r;
        });

        ResultDto result = service.create(new ResultDto(999, 85, 10, null, "student-1"));

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.examId()).isEqualTo(10);
    }

    @Test
    void create_withBothExamAndAssignment_isRejected() {
        assertThatThrownBy(() -> service.create(new ResultDto(null, 85, 10, 20, "student-1")))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_withNeitherExamNorAssignment_isRejected() {
        assertThatThrownBy(() -> service.create(new ResultDto(null, 85, null, null, "student-1")))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }
}
