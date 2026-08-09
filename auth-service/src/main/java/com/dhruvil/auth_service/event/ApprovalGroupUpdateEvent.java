package com.dhruvil.auth_service.event;

import com.dhruvil.auth_service.enums.ApprovalGroupAction;
import lombok.*;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApprovalGroupUpdateEvent {

    private Long userId;

    private Long approvalGroupId;

    private ApprovalGroupAction action;

//    private Instant occurredAt;
}