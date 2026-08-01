package com.dhruvil.auth_service.controller;

import com.dhruvil.auth_service.dto.ApiResponse;
import com.dhruvil.auth_service.dto.ApprovalGroupRequest;
import com.dhruvil.auth_service.dto.ApprovalGroupResponse;
import com.dhruvil.auth_service.services.ApprovalGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/approval-groups")
@RequiredArgsConstructor
public class ApprovalGroupController {

    private final ApprovalGroupService approvalGroupService;

    @GetMapping
    @PreAuthorize("hasAuthority('GROUP_READ')")
    public ResponseEntity<ApiResponse<List<ApprovalGroupResponse>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>("Approval groups fetched successfully.", approvalGroupService.findAll()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('GROUP_READ')")
    public ResponseEntity<ApiResponse<List<ApprovalGroupResponse>>> search(
            @RequestParam(name = "query", required = false) String query
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Approval groups search completed successfully.", approvalGroupService.search(query)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GROUP_READ')")
    public ResponseEntity<ApiResponse<ApprovalGroupResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Approval group fetched successfully.", approvalGroupService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GROUP_WRITE')")
    public ResponseEntity<ApiResponse<ApprovalGroupResponse>> create(@Valid @RequestBody ApprovalGroupRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Approval group created successfully.", approvalGroupService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GROUP_WRITE')")
    public ResponseEntity<ApiResponse<ApprovalGroupResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalGroupRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Approval group updated successfully.", approvalGroupService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GROUP_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        approvalGroupService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("Approval group deleted successfully.", null));
    }

    @PostMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAuthority('GROUP_WRITE')")
    public ResponseEntity<ApiResponse<ApprovalGroupResponse>> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "User added to approval group successfully.",
                        approvalGroupService.addMember(groupId, userId)
                )
        );
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAuthority('GROUP_WRITE')")
    public ResponseEntity<ApiResponse<ApprovalGroupResponse>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "User removed from approval group successfully.",
                        approvalGroupService.removeMember(groupId, userId)
                )
        );
    }
}
