package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.dto.PermissionRequest;
import com.dhruvil.auth_service.dto.PermissionResponse;
import com.dhruvil.auth_service.entity.Permission;
import com.dhruvil.auth_service.exception.DuplicateResourceException;
import com.dhruvil.auth_service.exception.ResourceNotFoundException;
import com.dhruvil.auth_service.repository.PermissionRepository;
import com.dhruvil.auth_service.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionCacheService permissionCacheService;

    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(Long id) {
        return toResponse(getPermission(id));
    }

    public PermissionResponse create(PermissionRequest request) {
        String name = normalizeName(request.name());
        if (permissionRepository.existsByName(name)) {
            throw new DuplicateResourceException("Permission already exists: " + name);
        }

        Permission permission = permissionRepository.save(
                Permission.builder()
                        .name(name)
                        .description(request.description())
                        .build()
        );

        return toResponse(permission);
    }

    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission permission = getPermission(id);
        String name = normalizeName(request.name());

        permissionRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Permission already exists: " + name);
                });

        permission.setName(name);
        permission.setDescription(request.description());
        permissionCacheService.evictRolePermissionCache();
        return toResponse(permissionRepository.save(permission));
    }

    public void delete(Long id) {
        Permission permission = getPermission(id);
        rolePermissionRepository.deleteByPermission(permission);
        permissionRepository.delete(permission);
        permissionCacheService.evictRolePermissionCache();
    }

    private Permission getPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase();
    }
}
