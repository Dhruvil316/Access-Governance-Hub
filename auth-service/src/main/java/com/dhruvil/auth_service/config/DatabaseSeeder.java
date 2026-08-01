package com.dhruvil.auth_service.config;

import com.dhruvil.auth_service.entity.ApprovalGroup;
import com.dhruvil.auth_service.entity.ApprovalGroupMember;
import com.dhruvil.auth_service.entity.Permission;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.RolePermission;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.entity.UserRole;
import com.dhruvil.auth_service.repository.ApprovalGroupMemberRepository;
import com.dhruvil.auth_service.repository.ApprovalGroupRepository;
import com.dhruvil.auth_service.repository.PermissionRepository;
import com.dhruvil.auth_service.repository.RolePermissionRepository;
import com.dhruvil.auth_service.repository.RoleRepository;
import com.dhruvil.auth_service.repository.UserRepository;
import com.dhruvil.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.seed.enabled",
        havingValue = "true"
)
@Transactional
public class DatabaseSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "Password@123";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ApprovalGroupRepository approvalGroupRepository;
    private final ApprovalGroupMemberRepository approvalGroupMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedPermissions();
        seedRolePermissions();
        seedApprovalGroups();
        seedUsers();
    }

    private void seedRoles() {
        createRole("EMPLOYEE", "Standard employee who can create requests and view own requests");
        createRole("APPROVER", "User who can view and act on assigned approval tasks");
        createRole("WORKFLOW_ADMIN", "Administrator for workflow and approval group configuration");
        createRole("SYSTEM_ADMIN", "System administrator with all permissions");
    }

    private void seedPermissions() {
        createPermission("REQUEST_CREATE", "Create access requests");
        createPermission("REQUEST_VIEW_OWN", "View own access requests");
        createPermission("REQUEST_VIEW_ALL", "View all access requests");
        createPermission("APPROVAL_VIEW", "View approval tasks assigned by workflow services");
        createPermission("APPROVAL_ACTION", "Act on approval tasks assigned by workflow services");
        createPermission("WORKFLOW_READ", "Read workflow configuration");
        createPermission("WORKFLOW_WRITE", "Write workflow configuration");
        createPermission("GROUP_READ", "Read approval groups");
        createPermission("GROUP_WRITE", "Write approval groups");
        createPermission("USER_READ", "Read users, roles, and permissions");
        createPermission("USER_WRITE", "Write users, roles, and permissions");
    }

    private void seedRolePermissions() {
        assignPermissions("EMPLOYEE", List.of(
                "REQUEST_CREATE",
                "REQUEST_VIEW_OWN"
        ));

        assignPermissions("APPROVER", List.of(
                "APPROVAL_VIEW",
                "APPROVAL_ACTION"
        ));

        assignPermissions("WORKFLOW_ADMIN", List.of(
                "WORKFLOW_READ",
                "WORKFLOW_WRITE",
                "GROUP_READ",
                "GROUP_WRITE"
        ));

        assignPermissions("SYSTEM_ADMIN", List.of(
                "REQUEST_CREATE",
                "REQUEST_VIEW_OWN",
                "REQUEST_VIEW_ALL",
                "APPROVAL_VIEW",
                "APPROVAL_ACTION",
                "WORKFLOW_READ",
                "WORKFLOW_WRITE",
                "GROUP_READ",
                "GROUP_WRITE",
                "USER_READ",
                "USER_WRITE"
        ));
    }

    private void seedApprovalGroups() {
        createApprovalGroup("Data Governance Team", "Approvers for data access governance");
        createApprovalGroup("Procurement Approval Group", "Approvers for procurement-related access decisions");
        createApprovalGroup("Privileged Access Review Board", "Cross-functional group for privileged access review");
    }

    private void seedUsers() {

        User dhruvil = createUser(
                "EMP-9999" ,
                "Dhruvil",
                "Rana",
                "work.dhruvilrana@gmail.com",
                "System Administrator",
                "System Administrator"
        );

        assignRole(dhruvil , "SYSTEM_ADMIN");

        User rahul = createUser(
                "EMP-1001",
                "Rahul",
                "Patel",
                "rahul.patel@example.com",
                "Engineering",
                "Software Engineer"
        );
        assignRole(rahul, "EMPLOYEE");

        User aarav = createUser(
                "EMP-1002",
                "Aarav",
                "Sharma",
                "aarav.sharma@example.com",
                "Data Governance",
                "Data Governance Manager"
        );
        assignRole(aarav, "EMPLOYEE");
        assignRole(aarav, "APPROVER");
        addToApprovalGroup(aarav, "Data Governance Team");

        User priya = createUser(
                "EMP-1003",
                "Priya",
                "Mehta",
                "priya.mehta@example.com",
                "Procurement",
                "Procurement Manager"
        );
        assignRole(priya, "EMPLOYEE");
        assignRole(priya, "APPROVER");
        addToApprovalGroup(priya, "Procurement Approval Group");

        User vikram = createUser(
                "EMP-1004",
                "Vikram",
                "Shah",
                "vikram.shah@example.com",
                "Finance",
                "Finance Controller"
        );
        assignRole(vikram, "EMPLOYEE");
        assignRole(vikram, "APPROVER");
        addToApprovalGroup(vikram, "Procurement Approval Group");

        User sneha = createUser(
                "EMP-1005",
                "Sneha",
                "Kapoor",
                "sneha.kapoor@example.com",
                "Security",
                "Security Lead"
        );
        assignRole(sneha, "EMPLOYEE");
        assignRole(sneha, "APPROVER");
        addToApprovalGroup(sneha, "Privileged Access Review Board");

        User arjun = createUser(
                "EMP-1006",
                "Arjun",
                "Nair",
                "arjun.nair@example.com",
                "Infrastructure",
                "Infrastructure Manager"
        );
        assignRole(arjun, "EMPLOYEE");
        assignRole(arjun, "APPROVER");
        addToApprovalGroup(arjun, "Privileged Access Review Board");
    }

    private Role createRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        ));
    }

    private Permission createPermission(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() ->
                        permissionRepository.save(
                                Permission.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        ));
    }

    private ApprovalGroup createApprovalGroup(String name, String description) {
        return approvalGroupRepository.findByName(name)
                .orElseGet(() ->
                        approvalGroupRepository.save(
                                ApprovalGroup.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        ));
    }

    private User createUser(
            String employeeId,
            String firstName,
            String lastName,
            String email,
            String department,
            String designation
    ) {
        return userRepository.findByEmail(email)
                .orElseGet(() ->
                        userRepository.save(
                                User.builder()
                                        .employeeId(employeeId)
                                        .firstName(firstName)
                                        .lastName(lastName)
                                        .email(email)
                                        .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                                        .department(department)
                                        .designation(designation)
                                        .enabled(true)
                                        .build()
                        ));
    }

    private void assignPermissions(String roleName, List<String> permissionNames) {
        Role role = roleRepository.findByName(roleName).orElseThrow();

        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName).orElseThrow();
            if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
                rolePermissionRepository.save(
                        RolePermission.builder()
                                .role(role)
                                .permission(permission)
                                .build()
                );
            }
        }
    }

    private void assignRole(User user, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        if (!userRoleRepository.existsByUserAndRole(user, role)) {
            userRoleRepository.save(
                    UserRole.builder()
                            .user(user)
                            .role(role)
                            .build()
            );
        }
    }

    private void addToApprovalGroup(User user, String approvalGroupName) {
        ApprovalGroup approvalGroup = approvalGroupRepository.findByName(approvalGroupName).orElseThrow();
        if (!approvalGroupMemberRepository.existsByApprovalGroupAndUser(approvalGroup, user)) {
            approvalGroupMemberRepository.save(
                    ApprovalGroupMember.builder()
                            .approvalGroup(approvalGroup)
                            .user(user)
                            .build()
            );
        }
    }
}
