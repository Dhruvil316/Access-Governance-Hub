package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.dto.AuthorityInfo;
import com.dhruvil.auth_service.dto.UserCreateRequest;
import com.dhruvil.auth_service.dto.UserResponse;
import com.dhruvil.auth_service.dto.UserSummaryResponse;
import com.dhruvil.auth_service.dto.UserUpdateRequest;
import com.dhruvil.auth_service.entity.ApprovalGroupMember;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.entity.UserRole;
import com.dhruvil.auth_service.exception.DuplicateResourceException;
import com.dhruvil.auth_service.exception.ResourceNotFoundException;
import com.dhruvil.auth_service.repository.ApprovalGroupMemberRepository;
import com.dhruvil.auth_service.repository.RoleRepository;
import com.dhruvil.auth_service.repository.UserRepository;
import com.dhruvil.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ApprovalGroupMemberRepository approvalGroupMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityService authorityService;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> search(String query) {
        String normalizedQuery = Optional.ofNullable(query).orElse("").trim();
        if (normalizedQuery.isBlank()) {
            return findAll();
        }

        return userRepository.search(normalizedQuery)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUser(id));
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponse(user);
    }

    public UserResponse create(UserCreateRequest request) {
        validateUniqueUser(request.employeeId(), request.email(), null);

        User user = User.builder()
                .employeeId(request.employeeId().trim())
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .department(request.department().trim())
                .designation(request.designation().trim())
                .enabled(request.enabled())
                .build();

        User savedUser = userRepository.save(user);

        List<String> roles = Optional.ofNullable(request.roles()).orElse(List.of("EMPLOYEE"));
        for (String roleName : roles) {
            assignRole(savedUser.getId(), roleName);
        }

        return toResponse(savedUser);
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        validateUniqueUser(request.employeeId(), request.email(), id);

        user.setEmployeeId(request.employeeId().trim());
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setDepartment(request.department().trim());
        user.setDesignation(request.designation().trim());
        user.setEnabled(request.enabled());

        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = getUser(id);
        refreshTokenService.deleteAllUserTokens(user);
        approvalGroupMemberRepository.deleteByUser(user);
        userRoleRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    public UserResponse assignRole(Long userId, Long roleId) {
        User user = getUser(userId);
        Role role = getRole(roleId);
        assignRole(user, role);
        return toResponse(user);
    }

    public UserResponse removeRole(Long userId, Long roleId) {
        User user = getUser(userId);
        Role role = getRole(roleId);

        userRoleRepository.findByUserAndRole(user, role)
                .ifPresent(userRoleRepository::delete);

        return toResponse(user);
    }

    User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    UserSummaryResponse toSummary(User user) {
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

    UserResponse toResponse(User user) {
        AuthorityInfo authorityInfo = authorityService.getAuthorities(user);
        List<String> approvalGroups = approvalGroupMemberRepository.findAllGroupsByUser(user)
                .stream()
                .map(ApprovalGroupMember::getApprovalGroup)
                .map(group -> group.getName())
                .toList();

        return new UserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDepartment(),
                user.getDesignation(),
                user.getEnabled(),
                authorityInfo.getRoles(),
                authorityInfo.getPermissions(),
                approvalGroups,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private void assignRole(Long userId, String roleName) {
        User user = getUser(userId);
        Role role = roleRepository.findByName(normalizeName(roleName))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        assignRole(user, role);
    }

    private void assignRole(User user, Role role) {
        if (!userRoleRepository.existsByUserAndRole(user, role)) {
            userRoleRepository.save(
                    UserRole.builder()
                            .user(user)
                            .role(role)
                            .build()
            );
        }
    }

    private Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private void validateUniqueUser(String employeeId, String email, Long currentUserId) {
        userRepository.findByEmployeeId(employeeId.trim())
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("User already exists with employeeId: " + employeeId);
                });

        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("User already exists with email: " + email);
                });
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase();
    }
}
