package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Transport shape for a result. Exactly one of examId/assignmentId is expected to be set; both
 * are nullable here (the cross-field rule is enforced in the Phase 3 service).
 */
public record ResultDto(
        Integer id,
        @NotNull @PositiveOrZero Integer score,
        Integer examId,
        Integer assignmentId,
        @NotBlank @Size(max = 255) String studentId) {
}
