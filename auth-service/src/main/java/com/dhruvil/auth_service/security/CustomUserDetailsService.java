package com.dhruvil.auth_service.security;

import com.dhruvil.auth_service.entity.RolePermission;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.entity.UserRole;
import com.dhruvil.auth_service.repository.RolePermissionRepository;
import com.dhruvil.auth_service.repository.UserRepository;
import com.dhruvil.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/*
    If the user does not exist then throw error
    This simply take out the user and convert it into the UserDetails so that spring
    can easily interpret the user and compare the password

    Later when request comes with JWT
    Authorization: Bearer xxxxx

    JWT filter does
    Extract email
        ↓
    CustomUserDetailsService.loadUserByUsername(email)
        ↓
    Validate JWT
        ↓
    SecurityContextHolder
*/

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email)
            throws UsernameNotFoundException {

        // 1. Load User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email : " + email));

        // 2. Load User Roles
        List<UserRole> userRoles = userRoleRepository.findAllRolesByUser(user);

        // 3. Collect Role IDs
        List<Long> roleIds = userRoles.stream()
                .map(userRole -> userRole.getRole().getId())
                .toList();

        // 4. Load Permissions for all Roles
        List<RolePermission> rolePermissions = roleIds.isEmpty()
                ? List.of()
                : rolePermissionRepository.findAllByRoleIds(roleIds);

        // 5. Build Authorities
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add Roles
        for (UserRole userRole : userRoles) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + userRole.getRole().getName()
                    )
            );
        }

        // Add Permissions
        for (RolePermission rolePermission : rolePermissions) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            rolePermission.getPermission().getName()
                    )
            );
        }

        // 6. Return UserPrincipal
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                authorities
        );
    }
}