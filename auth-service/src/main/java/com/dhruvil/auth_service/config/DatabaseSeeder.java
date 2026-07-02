package com.dhruvil.auth_service.config;

import com.dhruvil.auth_service.entity.Permission;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.RolePermission;
import com.dhruvil.auth_service.repository.PermissionRepository;
import com.dhruvil.auth_service.repository.RolePermissionRepository;
import com.dhruvil.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//@Component
@RequiredArgsConstructor
//@Transactional
public class DatabaseSeeder  {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

//    @Override
//    public void run(String... args) {
//
//        seedRoles();
//
//        seedPermissions();
//
//        seedRolePermissions();
//    }

    private void seedRoles() {

        createRole(
                "ADMIN",
                "Administrator with full system access"
        );

        createRole(
                "MANAGER",
                "Manager role"
        );

        createRole(
                "USER",
                "Default user role"
        );
    }

    private void seedPermissions() {

        createPermission(
                "USER_READ",
                "Read users"
        );

        createPermission(
                "USER_CREATE",
                "Create users"
        );

        createPermission(
                "USER_UPDATE",
                "Update users"
        );

        createPermission(
                "USER_DELETE",
                "Delete users"
        );

        createPermission(
                "ROLE_READ",
                "Read roles"
        );

        createPermission(
                "ROLE_CREATE",
                "Create roles"
        );

        createPermission(
                "ROLE_UPDATE",
                "Update roles"
        );

        createPermission(
                "ROLE_DELETE",
                "Delete roles"
        );
    }

    private void seedRolePermissions() {

        Role admin = roleRepository.findByName("ADMIN").orElseThrow();

        Role manager = roleRepository.findByName("MANAGER").orElseThrow();

        Role user = roleRepository.findByName("USER").orElseThrow();

        // ADMIN
        assign(admin, "USER_READ");
        assign(admin, "USER_CREATE");
        assign(admin, "USER_UPDATE");
        assign(admin, "USER_DELETE");

        assign(admin, "ROLE_READ");
        assign(admin, "ROLE_CREATE");
        assign(admin, "ROLE_UPDATE");
        assign(admin, "ROLE_DELETE");

        // MANAGER
        assign(manager, "USER_READ");
        assign(manager, "USER_UPDATE");

        // USER
        assign(user, "USER_READ");
    }

    private Role createRole(
            String name,
            String description
    ) {

        return roleRepository.findByName(name)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        ));
    }

    private Permission createPermission(
            String name,
            String description
    ) {

        return permissionRepository.findByName(name)
                .orElseGet(() ->
                        permissionRepository.save(
                                Permission.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        ));
    }

    private void assign(
            Role role,
            String permissionName
    ) {

        Permission permission = permissionRepository
                .findByName(permissionName)
                .orElseThrow();

        if (!rolePermissionRepository.existsByRoleAndPermission(
                role,
                permission
        )) {

            rolePermissionRepository.save(
                    RolePermission.builder()
                            .role(role)
                            .permission(permission)
                            .build()
            );
        }
    }
}