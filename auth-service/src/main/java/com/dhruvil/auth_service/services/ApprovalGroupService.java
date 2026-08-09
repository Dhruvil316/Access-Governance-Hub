package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.dto.ApprovalGroupRequest;
import com.dhruvil.auth_service.dto.ApprovalGroupResponse;
import com.dhruvil.auth_service.dto.UserSummaryResponse;
import com.dhruvil.auth_service.entity.ApprovalGroup;
import com.dhruvil.auth_service.entity.ApprovalGroupMember;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.exception.DuplicateResourceException;
import com.dhruvil.auth_service.exception.ResourceNotFoundException;
import com.dhruvil.auth_service.repository.ApprovalGroupMemberRepository;
import com.dhruvil.auth_service.repository.ApprovalGroupRepository;
import com.dhruvil.auth_service.repository.UserRepository;
import com.dhruvil.event.ApprovalGroupAction;
import com.dhruvil.event.ApprovalGroupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalGroupService {

    @Value("${kafka.topic.approval-group-topic}")
    private String KAFKA_APPROVAL_GROUP_TOPIC ;

    private final ApprovalGroupRepository approvalGroupRepository;
    private final ApprovalGroupMemberRepository approvalGroupMemberRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<Long, ApprovalGroupEvent> kafkaTemplate ;

    @Transactional(readOnly = true)
    public List<ApprovalGroupResponse> findAll() {
        return approvalGroupRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalGroupResponse> search(String query) {
        String normalizedQuery = Optional.ofNullable(query).orElse("").trim();
        if (normalizedQuery.isBlank()) {
            return findAll();
        }

        return approvalGroupRepository.search(normalizedQuery)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApprovalGroupResponse findById(Long id) {
        return toResponse(getApprovalGroup(id));
    }

    public ApprovalGroupResponse create(ApprovalGroupRequest request) {
        String name = request.name().trim();
        if (approvalGroupRepository.existsByName(name)) {
            throw new DuplicateResourceException("Approval group already exists: " + name);
        }

        ApprovalGroup approvalGroup = approvalGroupRepository.save(
                ApprovalGroup.builder()
                        .name(name)
                        .description(request.description())
                        .build()
        );

        return toResponse(approvalGroup);
    }

    public ApprovalGroupResponse update(Long id, ApprovalGroupRequest request) {
        ApprovalGroup approvalGroup = getApprovalGroup(id);
        String name = request.name().trim();

        approvalGroupRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Approval group already exists: " + name);
                });

        approvalGroup.setName(name);
        approvalGroup.setDescription(request.description());
        return toResponse(approvalGroupRepository.save(approvalGroup));
    }

    public void delete(Long id) {
        ApprovalGroup approvalGroup = getApprovalGroup(id);
        approvalGroupMemberRepository.deleteByApprovalGroup(approvalGroup);
        approvalGroupRepository.delete(approvalGroup);
    }

    public ApprovalGroupResponse addMember(Long groupId, Long userId) {
        ApprovalGroup approvalGroup = getApprovalGroup(groupId);
        User user = getUser(userId);

        if (!approvalGroupMemberRepository.existsByApprovalGroupAndUser(approvalGroup, user)) {
            approvalGroupMemberRepository.save(
                    ApprovalGroupMember.builder()
                            .approvalGroup(approvalGroup)
                            .user(user)
                            .build()
            );

            kafkaTemplate.send(
                    KAFKA_APPROVAL_GROUP_TOPIC,
                    groupId,
                    ApprovalGroupEvent.newBuilder()
                            .setApprovalGroupId(groupId)
                            .setUserId(userId)
                            .setAction(ApprovalGroupAction.MEMBER_ADDED)
                            .build()
            );
        }

        return toResponse(approvalGroup);
    }

    public ApprovalGroupResponse removeMember(Long groupId, Long userId) {
        ApprovalGroup approvalGroup = getApprovalGroup(groupId);
        User user = getUser(userId);

        approvalGroupMemberRepository.findByApprovalGroupAndUser(approvalGroup, user)
                .ifPresent(approvalGroupMemberRepository::delete);

        return toResponse(approvalGroup);
    }

    private ApprovalGroup getApprovalGroup(Long id) {
        return approvalGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval group not found with id: " + id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private ApprovalGroupResponse toResponse(ApprovalGroup approvalGroup) {
        List<UserSummaryResponse> members = approvalGroupMemberRepository.findAllMembersByApprovalGroup(approvalGroup)
                .stream()
                .map(ApprovalGroupMember::getUser)
                .map(this::toUserSummary)
                .toList();

        return new ApprovalGroupResponse(
                approvalGroup.getId(),
                approvalGroup.getName(),
                approvalGroup.getDescription(),
                members,
                approvalGroup.getCreatedAt(),
                approvalGroup.getUpdatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDepartment(),
                user.getDesignation(),
                user.getEnabled()
        );
    }
}
