package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Transport shape for a grade level.
 */
public record GradeDto(
        Integer id,
        @NotNull @Positive Integer level) {
}
