package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for an exam. The last four fields are read-only display enrichment resolved from
 * the exam's lesson (lesson name, its subject and class names, and the teacher id/username); they
 * are populated on reads and ignored on writes.
 */
public record ExamDto(
        Integer id,
        @NotBlank @Size(max = 255) String title,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotNull Integer lessonId,
        String lessonName,
        String subjectName,
        String className,
        String teacherId) {
}
