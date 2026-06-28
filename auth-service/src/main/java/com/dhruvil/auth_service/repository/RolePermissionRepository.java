package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.Permission;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Role role);

    boolean existsByRoleAndPermission(Role role, Permission permission);


    @Query("""
            SELECT rp
            FROM RolePermission rp
            JOIN FETCH rp.permission
            WHERE rp.role.id IN :roleIds
            """)
    List<RolePermission> findAllByRoleIds(List<Long> roleIds);
}