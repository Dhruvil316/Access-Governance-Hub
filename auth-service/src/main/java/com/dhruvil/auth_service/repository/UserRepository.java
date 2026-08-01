package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.department) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.designation) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY u.firstName, u.lastName
            """)
    List<User> search(String query);
}
