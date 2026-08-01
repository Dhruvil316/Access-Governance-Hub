package com.dhruvil.auth_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String employeeId,
        String firstName,
        String lastName,
        String email,
        String department,
        String designation,
        Boolean enabled,
        List<String> roles,
        List<String> permissions,
        List<String> approvalGroups,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
