package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.dto.RoleRequest;
import com.dhruvil.auth_service.dto.RoleResponse;
import com.dhruvil.auth_service.entity.Permission;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.RolePermission;
import com.dhruvil.auth_service.exception.DuplicateResourceException;
import com.dhruvil.auth_service.exception.ResourceNotFoundException;
import com.dhruvil.auth_service.repository.PermissionRepository;
import com.dhruvil.auth_service.repository.RolePermissionRepository;
import com.dhruvil.auth_service.repository.RoleRepository;
import com.dhruvil.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionCacheService permissionCacheService;

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return toResponse(getRole(id));
    }

    public RoleResponse create(RoleRequest request) {
        String name = normalizeName(request.name());
        if (roleRepository.existsByName(name)) {
            throw new DuplicateResourceException("Role already exists: " + name);
        }

        Role role = roleRepository.save(
                Role.builder()
                        .name(name)
                        .description(request.description())
                        .build()
        );

        return toResponse(role);
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role role = getRole(id);
        String name = normalizeName(request.name());

        roleRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Role already exists: " + name);
                });

        role.setName(name);
        role.setDescription(request.description());
        return toResponse(roleRepository.save(role));
    }

    public void delete(Long id) {
        Role role = getRole(id);
        userRoleRepository.deleteByRole(role);
        rolePermissionRepository.deleteByRole(role);
        roleRepository.delete(role);
        permissionCacheService.evictRolePermissionCache();
    }

    public RoleResponse assignPermission(Long roleId, Long permissionId) {
        Role role = getRole(roleId);
        Permission permission = getPermission(permissionId);

        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            rolePermissionRepository.save(
                    RolePermission.builder()
                            .role(role)
                            .permission(permission)
                            .build()
            );
            permissionCacheService.evictRolePermissionCache();
        }

        return toResponse(role);
    }

    public RoleResponse removePermission(Long roleId, Long permissionId) {
        Role role = getRole(roleId);
        Permission permission = getPermission(permissionId);

        rolePermissionRepository.findByRoleAndPermission(role, permission)
                .ifPresent(rolePermission -> {
                    rolePermissionRepository.delete(rolePermission);
                    permissionCacheService.evictRolePermissionCache();
                });

        return toResponse(role);
    }

    private Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private Permission getPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    private RoleResponse toResponse(Role role) {
        List<String> permissions = rolePermissionRepository.findAllPermissionsByRole(role)
                .stream()
                .map(RolePermission::getPermission)
                .map(Permission::getName)
                .toList();

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissions,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase();
    }
}
