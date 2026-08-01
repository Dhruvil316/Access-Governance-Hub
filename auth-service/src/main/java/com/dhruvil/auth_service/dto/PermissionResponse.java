package com.dhruvil.auth_service.dto;

import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
