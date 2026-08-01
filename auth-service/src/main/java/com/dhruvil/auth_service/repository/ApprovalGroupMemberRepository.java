package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.ApprovalGroup;
import com.dhruvil.auth_service.entity.ApprovalGroupMember;
import com.dhruvil.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApprovalGroupMemberRepository extends JpaRepository<ApprovalGroupMember, Long> {

    boolean existsByApprovalGroupAndUser(ApprovalGroup approvalGroup, User user);

    Optional<ApprovalGroupMember> findByApprovalGroupAndUser(ApprovalGroup approvalGroup, User user);

    void deleteByApprovalGroup(ApprovalGroup approvalGroup);

    void deleteByUser(User user);

    @Query("""
            SELECT agm
            FROM ApprovalGroupMember agm
            JOIN FETCH agm.user
            WHERE agm.approvalGroup = :approvalGroup
            ORDER BY agm.user.firstName, agm.user.lastName
            """)
    List<ApprovalGroupMember> findAllMembersByApprovalGroup(ApprovalGroup approvalGroup);

    @Query("""
            SELECT agm
            FROM ApprovalGroupMember agm
            JOIN FETCH agm.approvalGroup
            WHERE agm.user = :user
            ORDER BY agm.approvalGroup.name
            """)
    List<ApprovalGroupMember> findAllGroupsByUser(User user);
}
