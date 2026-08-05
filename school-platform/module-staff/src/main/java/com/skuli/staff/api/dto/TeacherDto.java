package com.skuli.staff.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skuli.common.domain.UserSex;
import com.skuli.common.validation.OnCreate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

/**
 * Transport shape for a teacher, mirroring the legacy Zod {@code teacherSchema}. {@code id} equals
 * the Keycloak username. {@code createdAt} is read-only (server-populated). {@code password} is
 * write-only (accepted on input, never serialised back) and required only on create — on update a
 * null password leaves the Keycloak credential unchanged.
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
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(groups = OnCreate.class)
        @Size(min = 8, max = 100, groups = OnCreate.class) String password,
        Instant createdAt) {
}
