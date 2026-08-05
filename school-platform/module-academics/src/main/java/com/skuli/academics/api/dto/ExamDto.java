package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for an exam.
 */
public record ExamDto(
        Integer id,
        @NotBlank @Size(max = 255) String title,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotNull Integer lessonId) {
}
