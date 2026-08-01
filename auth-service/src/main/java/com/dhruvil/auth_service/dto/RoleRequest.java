package com.dhruvil.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank String name,
        String description
) {
}
