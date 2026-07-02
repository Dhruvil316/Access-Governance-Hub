package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.dto.AuthorityInfo;
import com.dhruvil.auth_service.entity.RolePermission;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.entity.UserRole;
import com.dhruvil.auth_service.repository.RolePermissionRepository;
import com.dhruvil.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public AuthorityInfo getAuthorities(User user) {

        // find all user roles
        List<UserRole> userRoles = userRoleRepository.findAllRolesByUser(user);

        List<String> roles = userRoles.stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        List<Long> roleIds = userRoles.stream()
                .map(userRole -> userRole.getRole().getId())
                .toList();

        List<String> permissions = rolePermissionRepository
                .findAllByRoleIds(roleIds)
                .stream()
                .map(rolePermission -> rolePermission.getPermission().getName())
                .distinct()
                .toList();


        return AuthorityInfo.builder()
                .roles(roles)
                .permissions(permissions)
                .build();
    }

}