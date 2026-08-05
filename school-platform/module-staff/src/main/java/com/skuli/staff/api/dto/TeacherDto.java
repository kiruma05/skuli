package com.skuli.staff.api.dto;

import com.skuli.common.domain.UserSex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

/**
 * Transport shape for a teacher, mirroring the legacy Zod {@code teacherSchema}. Used for both
 * reads and writes in Phase 2; {@code createdAt} is read-only (server-populated) and ignored on
 * the write path. Validation constraints mirror the source form rules.
 */
public record TeacherDto(
        @NotBlank @Size(max = 255) String id,
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String surname,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String phone,
        @NotBlank @Size(max = 500) String address,
        @Size(max = 500) String img,
        @NotBlank @Size(max = 255) String bloodType,
        @NotNull UserSex sex,
        @NotNull Instant birthday,
        Set<Integer> subjectIds,
        Instant createdAt) {
}
