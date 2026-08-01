package com.dhruvil.auth_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RoleResponse(
        Long id,
        String name,
        String description,
        List<String> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
