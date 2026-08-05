package com.skuli.staff.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Transport shape for an administrator.
 */
public record AdminDto(
        @NotBlank @Size(max = 255) String id,
        @NotBlank @Size(max = 255) String username) {
}
