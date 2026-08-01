package com.dhruvil.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalGroupRequest(
        @NotBlank String name,
        String description
) {
}
