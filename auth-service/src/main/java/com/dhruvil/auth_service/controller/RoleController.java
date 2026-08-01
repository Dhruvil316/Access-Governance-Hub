package com.dhruvil.auth_service.controller;

import com.dhruvil.auth_service.dto.ApiResponse;
import com.dhruvil.auth_service.dto.RoleRequest;
import com.dhruvil.auth_service.dto.RoleResponse;
import com.dhruvil.auth_service.services.RoleManagementService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleManagementService roleManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>("Roles fetched successfully.", roleManagementService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<RoleResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Role fetched successfully.", roleManagementService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Role created successfully.", roleManagementService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Role updated successfully.", roleManagementService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleManagementService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("Role deleted successfully.", null));
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Permission assigned to role successfully.",
                        roleManagementService.assignPermission(roleId, permissionId)
                )
        );
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Permission removed from role successfully.",
                        roleManagementService.removePermission(roleId, permissionId)
                )
        );
    }
}
