package com.skuli.student.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for a parent/guardian, mirroring the legacy Zod {@code parentSchema}. Phone is
 * required. {@code createdAt} is server-populated and read-only.
 */
public record ParentDto(
        @NotBlank @Size(max = 255) String id,
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String surname,
        @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 255) String phone,
        @NotBlank @Size(max = 500) String address,
        Instant createdAt) {
}
