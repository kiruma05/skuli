package com.skuli.student.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skuli.common.domain.UserSex;
import com.skuli.common.validation.OnCreate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Transport shape for a student, mirroring the legacy Zod {@code studentSchema}. {@code id} equals
 * the Keycloak username; parent, class and grade are carried as ids. {@code createdAt} is
 * server-populated and read-only. {@code password} is write-only and required only on create.
 */
public record StudentDto(
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
        @NotBlank @Size(max = 255) String parentId,
        @NotNull Integer classId,
        @NotNull Integer gradeId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(groups = OnCreate.class)
        @Size(min = 8, max = 100, groups = OnCreate.class) String password,
        Instant createdAt) {
}
