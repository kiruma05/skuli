package com.skuli.academics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Transport shape for a subject, mirroring the legacy Zod {@code subjectSchema}.
 */
public record SubjectDto(
        Integer id,
        @NotBlank @Size(max = 255) String name) {
}
