package com.skuli.academics.api.dto;

import com.skuli.common.domain.Day;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for a scheduled lesson. Subject, class and teacher are carried as ids.
 */
public record LessonDto(
        Integer id,
        @NotBlank @Size(max = 255) String name,
        @NotNull Day day,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotNull Integer subjectId,
        @NotNull Integer classId,
        @NotBlank @Size(max = 255) String teacherId) {
}
