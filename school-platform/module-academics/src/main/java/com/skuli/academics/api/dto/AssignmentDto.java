package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for an assignment.
 */
public record AssignmentDto(
        Integer id,
        @NotBlank @Size(max = 255) String title,
        @NotNull Instant startDate,
        @NotNull Instant dueDate,
        @NotNull Integer lessonId) {
}
