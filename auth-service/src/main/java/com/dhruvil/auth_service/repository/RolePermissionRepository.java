package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.Permission;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Role role);

    boolean existsByRoleAndPermission(Role role, Permission permission);

    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);

    void deleteByRole(Role role);

    void deleteByPermission(Permission permission);


    @Query("""
            SELECT rp
            FROM RolePermission rp
            JOIN FETCH rp.permission
            WHERE rp.role.id IN :roleIds
            """)
    List<RolePermission> findAllByRoleIds(List<Long> roleIds);

    @Query("""
            SELECT rp
            FROM RolePermission rp
            JOIN FETCH rp.permission
            WHERE rp.role = :role
            ORDER BY rp.permission.name
            """)
    List<RolePermission> findAllPermissionsByRole(Role role);
}
