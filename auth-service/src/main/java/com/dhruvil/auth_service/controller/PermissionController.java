package com.dhruvil.auth_service.controller;

import com.dhruvil.auth_service.dto.ApiResponse;
import com.dhruvil.auth_service.dto.PermissionRequest;
import com.dhruvil.auth_service.dto.PermissionResponse;
import com.dhruvil.auth_service.services.PermissionManagementService;
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
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionManagementService permissionManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>("Permissions fetched successfully.", permissionManagementService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<PermissionResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Permission fetched successfully.", permissionManagementService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Permission created successfully.", permissionManagementService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Permission updated successfully.", permissionManagementService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionManagementService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("Permission deleted successfully.", null));
    }
}
