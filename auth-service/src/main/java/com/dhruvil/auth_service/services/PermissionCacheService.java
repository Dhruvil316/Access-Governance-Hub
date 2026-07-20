package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final RolePermissionRepository rolePermissionRepository ;
    @Cacheable(
            value = "role-permissions",
            key = "#roleIds"
    )
    List <String> getPermissions (List <Long> roleIds )
    {
        System.out.println("Fetching permissions from DATABASE...");
        return rolePermissionRepository.findAllByRoleIds(roleIds)
                .stream()
                .map(rolePermission -> rolePermission.getPermission().getName())
                .distinct()
                .toList();
    }
}
