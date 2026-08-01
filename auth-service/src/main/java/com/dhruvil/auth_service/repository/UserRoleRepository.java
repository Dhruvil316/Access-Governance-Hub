package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long > {

    List<UserRole> findByUser(User user);

    boolean existsByUserAndRole(User user, Role role);

    Optional<UserRole> findByUserAndRole(User user, Role role);

    void deleteByUser(User user);

    void deleteByRole(Role role);

    @Query("""
    SELECT ur
    FROM UserRole ur
    JOIN FETCH ur.role
    WHERE ur.user = :user
    """)
    List<UserRole> findAllRolesByUser(User user);


}
