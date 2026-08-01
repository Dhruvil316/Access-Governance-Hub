package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.ApprovalGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApprovalGroupRepository extends JpaRepository<ApprovalGroup, Long> {

    Optional<ApprovalGroup> findByName(String name);

    boolean existsByName(String name);

    @Query("""
            SELECT ag
            FROM ApprovalGroup ag
            WHERE LOWER(ag.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(ag.description) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY ag.name
            """)
    List<ApprovalGroup> search(String query);
}
