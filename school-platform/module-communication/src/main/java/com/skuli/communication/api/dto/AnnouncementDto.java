package com.skuli.communication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for a school announcement. The class id is optional (null = school-wide).
 */
public record AnnouncementDto(
        Integer id,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull Instant date,
        Integer classId) {
}
