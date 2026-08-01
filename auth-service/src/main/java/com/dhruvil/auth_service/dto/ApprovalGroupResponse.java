package com.dhruvil.auth_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalGroupResponse(
        Long id,
        String name,
        String description,
        List<UserSummaryResponse> members,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
