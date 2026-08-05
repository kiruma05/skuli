package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Transport shape for a class group, mirroring the legacy Zod {@code classSchema}. The supervisor
 * (a teacher id) is optional.
 */
public record SchoolClassDto(
        Integer id,
        @NotBlank @Size(max = 255) String name,
        @NotNull @Positive Integer capacity,
        @Size(max = 255) String supervisorId,
        @NotNull Integer gradeId) {
}
