package com.dhruvil.auth_service.dto;

public record UserSummaryResponse(
        Long id,
        String employeeId,
        String firstName,
        String lastName,
        String email,
        String department,
        String designation,
        Boolean enabled
) {
}
