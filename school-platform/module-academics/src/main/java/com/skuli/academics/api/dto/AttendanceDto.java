package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for an attendance record.
 */
public record AttendanceDto(
        Integer id,
        @NotNull Instant date,
        boolean present,
        @NotBlank @Size(max = 255) String studentId,
        @NotNull Integer lessonId) {
}
